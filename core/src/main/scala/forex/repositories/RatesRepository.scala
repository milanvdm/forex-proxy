package forex.repositories

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import forex.domain.{ Pair, Rate }

object RatesRepository {

  def getCache[F[_]](implicit S: Sync[F]): F[RatesRepository[F]] =
    Ref
      .of[F, Map[Pair, Rate]](Map.empty)
      .map { cache =>
        new RatesCache[F](cache)
      }
}

trait RatesRepository[F[_]] {

  def get(pair: Pair): F[Rate]
  def update(rates: Set[Rate]): F[Unit]

}

class RatesCache[F[_]](
  cache: Ref[F, Map[Pair, Rate]]
)(
  implicit
  S: Sync[F]
) extends RatesRepository[F] {

  override def get(pair: Pair): F[Rate] = ???

  override def update(rates: Set[Rate]): F[Unit] = ???

}
