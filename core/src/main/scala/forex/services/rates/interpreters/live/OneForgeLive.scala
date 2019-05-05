package forex.services.rates.interpreters.live

import cats.effect.Sync
import cats.implicits._
import forex.config.OneForgeConfig
import forex.domain.{ Pair, Rate }
import forex.services.rates.{ Algebra, Error }
import io.circe.generic.auto._
import org.http4s.Status.Successful
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.{ DecodeFailure, Uri }

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

  private val apiRoot = "https://forex.1forge.com/1.0.3"
  private val quotesPath = "/quotes"

  override def get(pair: Pair): F[Rate] = ???

  override def getAllPairs: F[Set[Rate]] =
    for {
      rootUri ← S.fromEither[Uri](Uri.fromString(apiRoot))
      uri = rootUri
        .withPath(quotesPath)
        .withQueryParam("pairs", Pair.all)
        .withQueryParam("api_key", config.apiKey)

      pairs ← httpClient.get[Set[OneForgeSuccessResponse]](uri) {
        case Successful(response) ⇒
          response
            .as[Set[OneForgeSuccessResponse]]
            .recoverWith {
              case _: DecodeFailure ⇒
                response
                  .as[OneForgeErrorResponse]
                  .reject { case errorResponse ⇒ Error.OneForgeLookupFailed(errorResponse.message) }
                  .map(_ ⇒ Set.empty[OneForgeSuccessResponse])
                  .adaptError {
                    case _: DecodeFailure ⇒ Error.Internal("Failed to decode api response")
                  }
            }
        case r ⇒
          r.as[String]
            .reject { case errorResponse ⇒ Error.OneForgeLookupFailed(errorResponse) }
            .map(_ ⇒ Set.empty)
      }
    } yield pairs.map(toRate)

}