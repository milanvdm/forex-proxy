package forex.services.rates.interpreters.live

import cats.effect.IO
import forex.config.OneForgeConfig
import forex.domain._
import forex.services.RatesServices
import forex.services.rates.Error
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.{ HttpRoutes, Response, Uri }
import org.scalatest.{ FlatSpec, Matchers }

class OneForgeLiveSpec extends FlatSpec with Matchers with Http4sDsl[IO] {
  import OneForgeLiveSpec._

  private def mockHttpClient(response: IO[Response[IO]]): Client[IO] = {
    object PairsQueryParam extends QueryParamDecoderMatcher[String]("pairs")
    object ApiKeyQueryParam extends QueryParamDecoderMatcher[String]("api_key")

    Client.fromHttpApp[IO] {
      HttpRoutes
        .of[IO] {
          case GET -> Root / "quotes" :? PairsQueryParam(_) +& ApiKeyQueryParam(_) =>
            response
        }
        .orNotFound
    }
  }

  "getAllRates" should "return all Rates if Forge API responds correctly" in {

    val httpClient = mockHttpClient(Ok(successResponse))
    val ratesService = RatesServices.live[IO](config, httpClient)

    val expected =
      Set(
        Rate(
          Pair(Currency.EUR, Currency.USD),
          Price(1.12008),
          Timestamp.fromEpoch(1556958556)
        ),
        Rate(
          Pair(Currency.USD, Currency.JPY),
          Price(111.093),
          Timestamp.fromEpoch(1556958556)
        )
      )

    val response = ratesService.getAllRates.unsafeRunSync()

    response should contain theSameElementsAs expected

  }

  "getAllRates" should "return Error if Forge API responds with error message" in {

    val httpClient = mockHttpClient(Ok(errorResponse))
    val ratesService = RatesServices.live[IO](config, httpClient)

    val exception = intercept[Error.OneForgeLookupFailed] {
      ratesService.getAllRates.unsafeRunSync()
    }

    exception.message shouldBe "It broke"

  }

  "getAllRates" should "return Error if Forge API responds with random message" in {

    val httpClient = mockHttpClient(Ok("random"))
    val ratesService = RatesServices.live[IO](config, httpClient)

    val exception = intercept[Error.Internal] {
      ratesService.getAllRates.unsafeRunSync()
    }

    exception.reason shouldBe "Failed to decode api response"

  }

  "getAllRates" should "return Error if Forge API responds with 404" in {

    val httpClient = mockHttpClient(NotFound())
    val ratesService = RatesServices.live[IO](config, httpClient)

    val exception = intercept[Error.OneForgeLookupFailed] {
      ratesService.getAllRates.unsafeRunSync()
    }

    exception.status shouldBe 404

  }

}

object OneForgeLiveSpec {

  val config = OneForgeConfig(Uri.unsafeFromString("http://myservice"), "secret")

  val successResponse: Json =
    json"""
          [
            {
              "symbol": "EURUSD",
              "bid": 1.11997,
              "ask": 1.1202,
              "price": 1.12008,
              "timestamp": 1556958556
            },
            {
              "symbol": "USDJPY",
              "bid": 111.083,
              "ask": 111.103,
              "price": 111.093,
              "timestamp": 1556958556
            }
          ]
        """

  val errorResponse: Json =
    json"""
          {
            "error": true,
            "message": "It broke"
          }
        """

}
