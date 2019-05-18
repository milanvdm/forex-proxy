package forex.programs.rates

import cats.effect.IO
import forex.domain.{ Currency, Pair, Price, Rate, Timestamp }
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.{ Error => ProgramError }
import forex.repositories.RatesRepository
import forex.services.RatesService
import forex.services.rates.{ Error => ServiceError }
import org.scalatest.{ FlatSpec, Matchers }

class ProgramSpec extends FlatSpec with Matchers {
  import ProgramSpec._

  "get" should "return the found rate" in {
    val program = new Program[IO](
      testRatesService(IO.delay(Set(rate))),
      testRatesRepository(IO.delay(Option(rate)))
    )

    val result = program.get(getRatesRequest)

    result.unsafeRunSync shouldBe rate
  }

  it should "return RateNotFound if rate is None" in {
    val program = new Program[IO](
      testRatesService(IO.delay(Set(rate))),
      testRatesRepository(IO.delay(None))
    )

    val result = program.get(getRatesRequest)

    val thrown = the[ProgramError] thrownBy result.unsafeRunSync

    thrown shouldBe ProgramError.RateNotFound
  }

  it should "adapt service error correctly to RateLookupFailed" in {
    val serviceError = ServiceError.OneForgeLookupFailed("test", 404)

    val program = new Program[IO](
      testRatesService(IO.delay(Set(rate))),
      testRatesRepository(IO.raiseError(serviceError))
    )

    val result = program.get(getRatesRequest)

    val thrown = the[ProgramError] thrownBy result.unsafeRunSync

    thrown shouldBe ProgramError.RateLookupFailed("test", 404)
  }

}

object ProgramSpec {

  val from: Currency = Currency.EUR
  val to: Currency = Currency.USD
  val pair = Pair(Currency.EUR, Currency.USD)
  val rate = Rate(
    pair,
    Price(1.12008),
    Timestamp.fromEpoch(0)
  )

  def testRatesService(rates: IO[Set[Rate]]): RatesService[IO] = new RatesService[IO] {
    override def getAllRates: IO[Set[Rate]] = rates
  }

  def testRatesRepository(rate: IO[Option[Rate]]): RatesRepository[IO] = new RatesRepository[IO] {
    override def getOrUpdate(
      pair: Pair,
      update: () => IO[Map[Pair, Rate]]
    ): IO[Option[Rate]] = rate
  }

  val getRatesRequest = GetRatesRequest(from, to)

}
