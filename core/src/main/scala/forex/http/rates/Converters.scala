package forex.http.rates

import forex.domain._

object Converters {
  import Protocol._

  def toGetApiResponse(rate: Rate): GetApiResponse =
    GetApiResponse(
      from = rate.pair.from,
      to = rate.pair.to,
      price = rate.price,
      timestamp = rate.timestamp
    )
}
