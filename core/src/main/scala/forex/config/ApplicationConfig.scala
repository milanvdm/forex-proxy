package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
  http: HttpConfig,
  oneForge: OneForgeConfig
)

case class HttpConfig(
  host: String,
  port: Int,
  timeout: FiniteDuration
)

case class OneForgeConfig(
  apiRoot: String,
  apiKey: String
)
