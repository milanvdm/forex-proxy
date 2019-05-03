package forex.domain

import io.circe.{ Encoder, Json }

final case class Pair(
  from: Currency,
  to: Currency
)

object Pair {
  implicit val encoder: Encoder[Pair] = new Encoder[Pair] {
    def apply(pair: Pair): Json =
      Json.fromString(s"${pair.from.entryName}${pair.to.entryName}")
  }
}
