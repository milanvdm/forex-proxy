package forex.services.rates

sealed trait Error extends Throwable
object Error {
  final case class OneForgeLookupFailed(message: String) extends Error
}
