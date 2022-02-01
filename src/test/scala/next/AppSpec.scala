package next

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AppSpec extends AnyFlatSpec with Matchers with ScalatestRouteTest with Directives {

  behavior of "App"

  it should "function" in {
    Get("/") ~> App.routes(it => complete(it.toString)) ~> check {
      responseAs[String] shouldBe "1"
    }
  }

}
