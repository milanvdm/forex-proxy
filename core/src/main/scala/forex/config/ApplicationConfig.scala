package forex.config

import scala.concurrent.duration.FiniteDuration

import org.http4s.Uri

case class ApplicationConfig(
  http: HttpConfig,
  oneForge: OneForgeConfig,
  cache: CacheConfig
)

case class HttpConfig(
  host: String,
  port: Int,
  timeout: FiniteDuration
)

case class OneForgeConfig(
  apiRoot: Uri,
  apiKey: String
)

case class CacheConfig(timeToLive: FiniteDuration)
