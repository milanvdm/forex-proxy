package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.config.OneForgeConfig
import forex.services.rates.interpreters.dummy.OneForgeDummy
import forex.services.rates.interpreters.live.OneForgeLive
import org.http4s.client.Client

object Interpreters {

  def dummy[F[_]](implicit A: Applicative[F]): Algebra[F] =
    new OneForgeDummy[F]()

  def live[F[_]](
    config: OneForgeConfig,
    httpClient: Client[F]
  )(
    implicit
    S: Sync[F]
  ): Algebra[F] =
    new OneForgeLive[F](config, httpClient)

}
