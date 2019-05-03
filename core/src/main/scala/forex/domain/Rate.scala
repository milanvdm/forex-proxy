package forex.domain

case class Rate(
  pair: Pair,
  price: Price,
  timestamp: Timestamp
)
