package forex.services.rates.interpreters.dummy

import cats.Applicative
import forex.domain.{ Currency, Pair, Price, Rate, Timestamp }
import forex.services.rates.Algebra

class OneForgeDummy[F[_]](implicit A: Applicative[F]) extends Algebra[F] {

  override def getAllRates: F[Set[Rate]] =
    A.pure(
      Set(
        Rate(
          Pair(Currency.EUR, Currency.JPY),
          Price(BigDecimal(100)),
          Timestamp.now
        )
      )
    )
}
