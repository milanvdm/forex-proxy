package forex.config

import cats.effect.Sync
import fs2.Stream
import pureconfig.generic.auto._

object Config {

  def stream[F[_]](
    path: String
  )(
    implicit
    S: Sync[F]
  ): Stream[F, ApplicationConfig] =
    Stream.eval(
      S.delay(
        pureconfig.loadConfigOrThrow[ApplicationConfig](path)
      )
    )

}
