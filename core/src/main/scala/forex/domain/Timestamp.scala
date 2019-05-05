package forex.domain

import java.time.{ Instant, OffsetDateTime, ZoneId }

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def fromEpoch(epoch: Long): Timestamp =
    Timestamp(
      Instant
        .ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault)
        .toOffsetDateTime
    )
}
