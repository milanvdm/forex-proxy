package forex.domain

import java.time.OffsetDateTime

import org.scalatest.{ FlatSpec, Matchers }

class TimestampSpec extends FlatSpec with Matchers {

  "Timestamp.fromEpoch" should "create a correct timestamp" in {

    val expected = Timestamp(
      OffsetDateTime.parse("1970-01-01T00:20:34Z")
    )

    Timestamp.fromEpoch(1234).value.isEqual(expected.value) shouldBe true

  }

}
