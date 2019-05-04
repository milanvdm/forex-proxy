package forex.services.rates.interpreters.live

import forex.domain._
import forex.services.rates.interpreters.live.Protocol.OneForgeSuccessResponse

object Converters {

  def toRate(oneForgeResponse: OneForgeSuccessResponse): Rate =
    Rate(
      Pair(
        Currency.withName(oneForgeResponse.symbol.take(3)),
        Currency.withName(oneForgeResponse.symbol.takeRight(3))
      ),
      Price(BigDecimal(oneForgeResponse.price)),
      Timestamp.fromEpoch(oneForgeResponse.timestamp)
    )

}
