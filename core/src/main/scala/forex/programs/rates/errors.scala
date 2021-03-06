package forex.programs.rates

import forex.services.rates.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Throwable
  object Error {
    final case class Internal(reason: String) extends Error
    final case class RateLookupFailed(
      msg: String,
      status: Int
    ) extends Error
    final object RateNotFound extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.Internal(reason) => Error.Internal(reason)
    case RatesServiceError.OneForgeLookupFailed(msg, status) => Error.RateLookupFailed(msg, status)
  }
}
