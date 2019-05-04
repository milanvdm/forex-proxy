package forex.domain

import io.circe.{ Encoder, Json }

final case class Pair(
  from: Currency,
  to: Currency
)

object Pair {

  def all: Set[Pair] = {
    val currencies = Currency.values
    val pairs = for { from ← currencies; to ← currencies if from != to } yield Pair(from, to)
    pairs.toSet
  }

  implicit val encoder: Encoder[Pair] = new Encoder[Pair] {
    def apply(pair: Pair): Json =
      Json.fromString(s"${pair.from.entryName}${pair.to.entryName}")
  }

}
