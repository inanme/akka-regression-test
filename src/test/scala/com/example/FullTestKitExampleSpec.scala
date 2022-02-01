package com.example
import akka.http.scaladsl.model.{ HttpMethods, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class FullTestKitExampleSpec extends AnyWordSpecLike with Matchers with ScalatestRouteTest {

  val smallRoute =
    get {
      pathSingleSlash {
        complete {
          "Captain on the bridge!"
        }
      } ~
      path("ping") {
        complete("PONG!")
      }
    }

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> smallRoute ~> check {
        responseAs[String] shouldEqual "Captain on the bridge!"
      }
    }

    "return a 'PONG!' response for GET requests to /ping" in {
      Get("/ping") ~> smallRoute ~> check {
        responseAs[String] shouldEqual "PONG!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> smallRoute ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path with route" in {
      Put() ~> smallRoute ~> check {
        rejections should contain(MethodRejection(HttpMethods.GET))
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path with seal" in {
      Put() ~> Route.seal(smallRoute) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
