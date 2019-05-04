package forex.services.rates

import forex.domain.{ Pair, Rate }

trait Algebra[F[_]] {
  def get(pair: Pair): F[Rate]
  def getAllPairs: F[Set[Rate]]
}
