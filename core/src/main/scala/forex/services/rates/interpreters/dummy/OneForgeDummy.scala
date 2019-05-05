package forex.services.rates.interpreters.dummy

import cats.Applicative
import forex.domain.{ Pair, Price, Rate, Timestamp }
import forex.services.rates.Algebra

class OneForgeDummy[F[_]](implicit A: Applicative[F]) extends Algebra[F] {

  override def get(pair: Pair): F[Rate] =
    A.pure(
      Rate(pair, Price(BigDecimal(100)), Timestamp.now)
    )

  override def getAllRates = ???
}
