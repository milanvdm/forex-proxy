package forex.services.rates.interpreters.live

object Protocol {

  trait OneForgeResponse

  case class OneForgeSuccessResponse(
    symbol: String,
    price: Double,
    timestamp: Long
  ) extends OneForgeResponse

  case class OneForgeErrorResponse(
    error: Boolean,
    message: String
  ) extends OneForgeResponse

}
