package forex.services.rates

sealed trait Error extends Throwable
object Error {
  final case class Internal(reason: String) extends Error
  final case class OneForgeLookupFailed(
    message: String,
    status: Int
  ) extends Error
}
