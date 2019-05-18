package forex.programs.rates

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.option._
import forex.domain._
import forex.programs.rates.errors._
import forex.repositories.RatesRepository
import forex.services.RatesService
import forex.services.rates.{ Error => RatesServiceError }

class Program[F[_]](
  ratesService: RatesService[F],
  ratesRepository: RatesRepository[F]
)(
  implicit
  S: Sync[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Rate] =
    ratesRepository
      .getOrUpdate(
        Pair(request.from, request.to),
        () => ratesService.getAllRates.map(_.map(rate => rate.pair -> rate).toMap)
      )
      .flatMap(_.liftTo[F](Error.RateNotFound))
      .adaptError {
        case error: RatesServiceError => toProgramError(error)
      }

}

object Program {

  def apply[F[_]](
    ratesService: RatesService[F],
    ratesRepository: RatesRepository[F]
  )(
    implicit
    S: Sync[F]
  ): Algebra[F] = new Program[F](ratesService, ratesRepository)

}
