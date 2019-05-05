package forex.programs.rates

import cats.effect.Sync
import cats.syntax.monadError._
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
  A: Sync[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Rate] =
    ratesRepository
      .get(Pair(request.from, request.to))
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
