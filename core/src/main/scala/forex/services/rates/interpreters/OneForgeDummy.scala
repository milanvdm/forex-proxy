package forex.services.rates.interpreters

import cats.Applicative
import forex.domain.{ Pair, Price, Rate, Timestamp }
import forex.services.rates.Algebra

class OneForgeDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Pair): F[Rate] =
    Applicative[F].pure(
      Rate(pair, Price(BigDecimal(100)), Timestamp.now)
    )
}
