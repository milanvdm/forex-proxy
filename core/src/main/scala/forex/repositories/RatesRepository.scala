package forex.repositories

import scala.concurrent.duration.SECONDS

import cats.effect.concurrent.{ Ref, Semaphore }
import cats.effect.{ Clock, Concurrent, Sync }
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.config.CacheConfig
import forex.domain.{ Pair, Rate, Timestamp }

object RatesRepository {

  def getCache[F[_], G[_]](
    config: CacheConfig
  )(
    implicit
    S: Sync[F],
    Co: Concurrent[G],
    Cl: Clock[G]
  ): F[RatesRepository[G]] =
    for {
      semaphore <- Semaphore.in[F, G](1)
      cache <- Ref.in[F, G, Map[Pair, Rate]](Map.empty)
    } yield
      new RatesCache[F, G](
        config,
        semaphore,
        cache
      )
}

trait RatesRepository[G[_]] {

  def getOrUpdate(
    pair: Pair,
    update: () => G[Map[Pair, Rate]]
  ): G[Option[Rate]]

}

class RatesCache[F[_], G[_]](
  config: CacheConfig,
  semaphore: Semaphore[G],
  cache: Ref[G, Map[Pair, Rate]]
)(
  implicit
  Co: Concurrent[G],
  Cl: Clock[G]
) extends RatesRepository[G] {

  override def getOrUpdate(
    pair: Pair,
    update: () => G[Map[Pair, Rate]]
  ): G[Option[Rate]] =
    get(pair).flatMap(onExpired(_) {
      semaphore.withPermit {
        get(pair).flatMap(onExpired(_) {
          updateCache(update)(pair)
        })
      }
    })

  private def get(pair: Pair): G[Option[Rate]] = cache.get.map(_.get(pair))

  private def onExpired(rate: Option[Rate])(onExpired: G[Option[Rate]]): G[Option[Rate]] = {
    val expired = Cl.realTime(SECONDS).map { currentTime =>
      val now = Timestamp.fromEpoch(currentTime)
      rate.forall(_.timestamp.isBefore(now.minus(config.timeToLive)))
    }
    expired.flatMap { e =>
      if (e) onExpired else Co.pure(rate)
    }
  }

  private def updateCache(update: () => G[Map[Pair, Rate]])(pair: Pair): G[Option[Rate]] =
    for {
      rates <- update()
      _ <- cache.set(rates)
    } yield rates.get(pair)

}
