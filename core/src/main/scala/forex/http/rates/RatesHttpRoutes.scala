package forex.http.rates

import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import forex.programs.RatesProgram
import forex.programs.rates.errors.{ Error => RatesProgramError }
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]](
  ratesProgram: RatesProgram[F]
)(
  implicit
  S: Sync[F]
) extends Http4sDsl[F] {

  import Converters._
  import QueryParams._
  import errors._

  private val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root :? FromQueryParam(fromParam) +& ToQueryParam(toParam) =>
        val rates: F[Protocol.GetApiResponse] = for {
          from <- S.fromEither(fromParam.toRight(Error.ParseFailure("Unable to parse [from]")))
          to <- S.fromEither(toParam.toRight(Error.ParseFailure("Unable to parse [to]")))
          rates <- ratesProgram
            .get(RatesProgramProtocol.GetRatesRequest(from, to))
            .map(toGetApiResponse)
            .adaptError {
              case error: RatesProgramError => toApiError(error)
            }
        } yield rates

        rates
          .flatMap(Ok(_))
          .recoverWith {
            case error: Error.ParseFailure => BadRequest(error.msg)
            case error: Error.RateLookupFailed => InternalServerError(error.msg)
          }
    }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
