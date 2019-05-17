package forex.repositories

import scala.concurrent.duration._

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ Clock, ContextShift, IO, Timer }
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import forex.config.CacheConfig
import forex.domain.{ Currency, Pair, Price, Rate, Timestamp }
import org.scalatest.{ FlatSpec, Matchers }

class RatesRepositorySpec extends FlatSpec with Matchers {
  import RatesRepositorySpec._

  implicit val contextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.global)

  "getOrUpdate" should "correctly initialize with update" in {
    implicit val clock: Clock[IO] = createTestClock[IO](() => IO.pure(0))

    val ratesRepository = RatesRepository.getCache[IO, IO](config).unsafeRunSync

    val counter = Ref.of[IO, Int](0).unsafeRunSync

    val result = ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter))

    result.unsafeRunSync shouldBe Some(rate)
    counter.get.unsafeRunSync shouldBe 1
  }

  it should "correctly return non-expired value without calling update" in {
    implicit val clock: Clock[IO] =
      createTestClock[IO](() => IO.delay(lastUpdate.plus(ttl - 1.minute).value.toEpochSecond))

    val ratesRepository = RatesRepository.getCache[IO, IO](config).unsafeRunSync

    val counter = Ref.of[IO, Int](0).unsafeRunSync

    val result = for {
      _ <- ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter)) // init
      result <- ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter))
    } yield result

    result.unsafeRunSync shouldBe Some(rate)
    counter.get.unsafeRunSync shouldBe 1
  }

  it should "correctly call update on expired value" in {
    implicit val clock: Clock[IO] =
      createTestClock[IO](() => IO.delay(lastUpdate.plus(ttl + 1.minute).value.toEpochSecond))

    val ratesRepository = RatesRepository.getCache[IO, IO](config).unsafeRunSync

    val counter = Ref.of[IO, Int](0).unsafeRunSync

    val result = for {
      _ <- ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter)) // init
      result <- ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter))
    } yield result

    result.unsafeRunSync shouldBe Some(rate)
    counter.get.unsafeRunSync shouldBe 2
  }

  it should "only call update once with concurrent requests" in {
    implicit val clock: Clock[IO] =
      createTestClock[IO](() => IO.delay(lastUpdate.plus(ttl + 1.minute).value.toEpochSecond))

    val ratesRepository = RatesRepository.getCache[IO, IO](config).unsafeRunSync

    val counter = Ref.of[IO, Int](0).unsafeRunSync

    val result = for {
      _ <- ratesRepository.getOrUpdate(pair, () => updateFunction[IO](counter)) // init
      result <- (
        ratesRepository.getOrUpdate(pair, () => slowUpdateFunction[IO](counter)),
        ratesRepository.getOrUpdate(pair, () => slowUpdateFunction[IO](counter)),
        ratesRepository.getOrUpdate(pair, () => slowUpdateFunction[IO](counter))
      ).parMapN { (result, _, _) =>
        result
      }
    } yield result

    result.unsafeRunSync shouldBe Some(newRate)
    counter.get.unsafeRunSync shouldBe 2
  }

}

object RatesRepositorySpec {

  val ttl: FiniteDuration = 5.minutes
  val config = CacheConfig(ttl)

  val pair = Pair(Currency.EUR, Currency.USD)
  val lastUpdate: Timestamp = Timestamp.fromEpoch(1000)
  val rate = Rate(
    pair,
    Price(1.12008),
    lastUpdate
  )

  val rates: Map[Pair, Rate] = Map(pair -> rate)

  val newRate = Rate(
    pair,
    Price(1.12008),
    lastUpdate.plus(ttl + 1.minute)
  )

  val newRates: Map[Pair, Rate] = Map(pair -> newRate)

  def updateFunction[F[_]: Monad](counter: Ref[F, Int]): F[Map[Pair, Rate]] =
    counter
      .modify(current => (current + 1, current))
      .map(_ => rates)

  def slowUpdateFunction[F[_]: Monad: Timer](counter: Ref[F, Int]): F[Map[Pair, Rate]] =
    for {
      _ <- Timer[F].sleep(1.seconds)
      _ <- updateFunction(counter)
    } yield newRates

  def createTestClock[F[_]](timeFunction: () => F[Long]): Clock[F] = new Clock[F] {
    override def realTime(unit: TimeUnit): F[Long] = timeFunction()
    override def monotonic(unit: TimeUnit): F[Long] = ???
  }

}
