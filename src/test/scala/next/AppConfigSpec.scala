package next

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.data.NonEmptyList
import next.AppConfig.FruitBasket
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class AppConfigSpec
    extends AnyFlatSpec
    with Matchers
    with ScalatestRouteTest
    with Directives
    with EitherValues
    with ScalaCheckDrivenPropertyChecks {

  behavior of "app config"

  it should "do" in {
    AppConfig.p1.value shouldBe FruitBasket(apples = NonEmptyList.of(1, 2, 3))
  }

}
