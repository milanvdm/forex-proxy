package forex

import scala.concurrent.ExecutionContext

import cats.effect._
import cats.syntax.functor._
import forex.config._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  implicit val ec: ExecutionContext = ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)

}

class Application[F[_]](
  implicit
  CE: ConcurrentEffect[F],
  EC: ExecutionContext,
  T: Timer[F]
) {

  def stream: Stream[F, Unit] =
    for {
      config ← Config.stream("app")
      httpClient ← BlazeClientBuilder
        .apply(EC)
        .stream
      module = new Module[F](config, httpClient)
      _ ← BlazeServerBuilder[F]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(module.httpApp)
        .serve
    } yield ()

}
