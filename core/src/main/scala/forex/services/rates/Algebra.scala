package forex.services.rates

import forex.domain.Rate

trait Algebra[F[_]] {
  def getAllRates: F[Set[Rate]]
}
