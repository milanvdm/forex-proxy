package forex.http.rates

import forex.programs.rates.errors.{ Error => RatesProgramError }

object errors {

  sealed trait Error extends Throwable
  object Error {
    final case class Internal(reason: String) extends Error
    final case class ParseFailure(msg: String) extends Error
    final case class RateLookupFailed(
      msg: String,
      status: Int
    ) extends Error
  }

  def toApiError(error: RatesProgramError): Error = error match {
    case RatesProgramError.Internal(reason) => Error.Internal(reason)
    case RatesProgramError.RateLookupFailed(msg, status) => Error.RateLookupFailed(msg, status)
  }
}
