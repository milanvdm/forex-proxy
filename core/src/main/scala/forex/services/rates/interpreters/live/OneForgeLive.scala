package forex.services.rates.interpreters.live

import cats.effect.Sync
import cats.implicits._
import forex.config.OneForgeConfig
import forex.domain.{ Pair, Rate }
import forex.services.rates.{ Algebra, Error }
import io.circe.generic.auto._
import org.http4s.DecodeFailure
import org.http4s.Status.Successful
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client

class OneForgeLive[F[_]](
  config: OneForgeConfig,
  httpClient: Client[F]
)(
  implicit
  S: Sync[F]
) extends Algebra[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private val quotesPath = "/quotes"

  override def getAllRates: F[Set[Rate]] = {
    val uri = config.apiRoot
      .withPath(quotesPath)
      .withQueryParam("pairs", Pair.all)
      .withQueryParam("api_key", config.apiKey)

    for {
      pairs <- httpClient.get[Set[OneForgeSuccessResponse]](uri) { //TODO: fetch
        case Successful(response) =>
          response
            .as[Set[OneForgeSuccessResponse]]
            .recoverWith {
              case _: DecodeFailure =>
                response
                  .as[OneForgeErrorResponse]
                  .reject {
                    case errorResponse =>
                      Error.OneForgeLookupFailed(errorResponse.message, response.status.code)
                  }
                  .map(_ => Set.empty[OneForgeSuccessResponse])
                  .adaptError {
                    case _: DecodeFailure => Error.Internal("Failed to decode api response")
                  }
            }
        case response =>
          S.raiseError(
            Error.OneForgeLookupFailed(response.status.reason, response.status.code)
          )
      }
    } yield pairs.map(toRate)
  }

}
