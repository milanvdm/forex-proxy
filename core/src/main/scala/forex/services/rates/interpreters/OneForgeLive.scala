package forex.services.rates.interpreters

import cats.Applicative
import forex.config.OneForgeConfig
import forex.domain.{ Pair, Price, Rate, Timestamp }
import forex.services.rates.Algebra
import org.http4s.client.Client

class OneForgeLive[F[_]](
  config: OneForgeConfig,
  httpClient: Client[F]
)(
  implicit
  A: Applicative[F]
) extends Algebra[F] {

  override def get(pair: Pair): F[Rate] =
    A.pure(
      Rate(pair, Price(BigDecimal(100)), Timestamp.now)
    )
}
