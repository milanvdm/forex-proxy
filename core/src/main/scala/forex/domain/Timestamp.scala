package forex.domain

import java.time.{ Instant, OffsetDateTime, ZoneOffset }

import scala.concurrent.duration.FiniteDuration

case class Timestamp(value: OffsetDateTime) extends AnyVal {

  def isBefore(other: Timestamp): Boolean = value.isBefore(other.value)

  def plus(duration: FiniteDuration): Timestamp = Timestamp(
    value.plusNanos(duration.toNanos)
  )

  def minus(duration: FiniteDuration): Timestamp = Timestamp(
    value.minusNanos(duration.toNanos)
  )

}

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def fromEpoch(epoch: Long): Timestamp =
    Timestamp(
      Instant
        .ofEpochSecond(epoch)
        .atZone(ZoneOffset.UTC)
        .toOffsetDateTime
    )
}
