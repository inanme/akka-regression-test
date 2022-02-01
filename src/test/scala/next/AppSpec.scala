package next

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import next.CirceSupport._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.global
class AppSpec extends AnyFlatSpec with Matchers with ScalatestRouteTest with Directives {

  behavior of "App"

  //implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  it should "function" in {
    Get("/") ~!> App.getInt(global)(it => complete(it.toString)) ~> check {
      handled shouldBe true
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "1"
    }
  }

  it should "get the user list" in {
    Get("/s") ~> App.getSignups(it => complete(it)) ~> check {
      handled shouldBe true
      responseAs[List[Signup]] shouldBe List.empty[Signup]
    }
  }

}
