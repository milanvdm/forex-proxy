package forex.domain

import org.scalatest.{ FlatSpec, Matchers }

class PairSpec extends FlatSpec with Matchers {

  "Pair.all" should "create all pair combinations" in {

    Pair.all.size shouldBe (Currency.values.size * Currency.values.size - Currency.values.size)

  }

}
