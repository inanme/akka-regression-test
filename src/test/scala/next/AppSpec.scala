package next

import akka.actor.Scheduler
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.headers.{ Accept, `Content-Type` }
import akka.http.scaladsl.model.{ ContentTypes, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{
  CircuitBreakerOpenRejection,
  Directives,
  Route,
  ValidationRejection
}
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.util.UUID
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._

class AppSpec
    extends AnyFlatSpec
    with Matchers
    with ScalatestRouteTest
    with Directives
    with ScalaCheckDrivenPropertyChecks {

  behavior of "App"

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  val genSignup: Gen[Signup] = for {
    username <- Gen.alphaStr
    password <- Gen.alphaStr
  } yield Signup(username, password)

  it should "function" in {
    Get() ~!> App.getInt(global)(it => complete(it.toString)) ~> check {
      handled shouldBe true
      response.status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "1"
    }
  }

  it should "getOptionInt" in {
    Get() ~> App.getOptionInt(
      _.fold(complete(StatusCodes.NotFound))(it => complete(StatusCodes.OK, it.toString))
    ) ~> check {
      handled shouldBe true
      response.status shouldBe StatusCodes.NotFound
    }
  }

  it should "get single signup" in {
    Get() ~> App.getSignup(complete(_)) ~> check {
      handled shouldBe true
      responseAs[Signup] shouldBe App.aSignup
      header[`Content-Type`] shouldBe Some(`Content-Type`(App.`application/vnd.app.v1+json`))
    }
  }

  it should "maybe signup" in {
    Get() ~> App.getMaybeSignup(App.completeOptional) ~> check {
      println(response)
      responseAs[String] shouldBe ""
      contentType shouldBe ContentTypes.NoContentType
      handled shouldBe true
      status shouldBe StatusCodes.NotFound
    }
  }

  it should "try signup" in {
    Get() ~> App.getTrySignup(App.completeTry) ~> check {
      println(response)
      responseAs[Signup] shouldBe App.aSignup
      handled shouldBe true
      status shouldBe StatusCodes.OK
    }
  }

  it should "read header" in {
    ContentTypes.`application/json`
    MediaTypes.`application/json`
    Get() ~> Accept(MediaTypes.`text/plain`) ~> headerValueByType(Accept)(it =>
      complete(it.value())
    ) ~> check {
      responseAs[String] shouldBe "text/plain"
      handled shouldBe true
      status shouldBe StatusCodes.OK
    }
  }

  it should "use rawroute" in {
    Get() ~> App.rawRoute ~> check {
      handled shouldBe true
      status shouldBe StatusCodes.OK
    }
  }

  it should "get the user list" in {
    Get() ~> App.getListSignup(complete(_)) ~> check {
      handled shouldBe true
      responseAs[List[Signup]] shouldBe List.empty[Signup]
      header[`Content-Type`] shouldBe Some(`Content-Type`(App.`application/vnd.app.v1+json`))
    }
  }

  it should "save signup" in {
    forAll(genSignup) { signup =>
      Post("/", signup) ~!>
      App.newSignup(complete(StatusCodes.Accepted, _)) ~> check {
        response.status shouldBe StatusCodes.Accepted
        responseAs[Signup] shouldBe signup
      }
    }
  }

  it should "get the uuid path parameter" in {
    val uuid = UUID.randomUUID()
    Get(s"/private/${uuid.toString}") ~> App.getPrivateUUID(it => complete(it.toString)) ~> check {
      responseAs[String] shouldBe uuid.toString
    }
  }

  it should "reject invalid uuid path parameter" in {
    Get(s"/private/thisisnotuuid") ~!> App.getPrivateUUID(it => complete(it.toString)) ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  it should "be rejected" in {
    Get() ~> App.getParamP { case (_, _) => complete("ok") } ~> check {
      rejections should contain(ValidationRejection("missing p"))
    }
  }

  it should "be rejected2" in {
    Get() ~> (handleRejections(App.MissingPRejectionHandler) & App.getParamP2) {
      case (_, _) => complete("ok")
    } ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[String] shouldBe "missing p"
    }
  }

  it should "handle exception" in {
    Get() ~> handleExceptions(App.AppExceptionHandler)(App.failing(complete("ok"))) ~> check {
      withClue("first") {
        status shouldBe StatusCodes.Conflict
      }
    }

    intercept[App.MissingPException.type] {
      handleExceptions(App.AppExceptionHandler) & App.failing
    }
  }

  it should "filter" in {
    Get() ~> (App.getInt.filter(_ > 1, ValidationRejection("less than 2")) & complete(
      "ok"
    )) ~> check {
      rejections should contain(ValidationRejection("less than 2"))
    }
  }

  it should "run with circuit breaker" in {
    implicit val scheduler: Scheduler = system.scheduler
    val route                         = AppWithCircuitBreaker.route
    Get("/divide/10/2") ~> route ~> check {
      responseAs[String] shouldEqual "The result was 5"
    }

    Get("/divide/10/0") ~> Route.seal(route) ~> check {
      status shouldEqual InternalServerError
      responseAs[String] shouldEqual "An error occurred: / by zero"
    } // opens the circuit breaker

    Get("/divide/10/2") ~> route ~> check {
      rejection shouldBe a[CircuitBreakerOpenRejection]
    }

    Thread.sleep(AppWithCircuitBreaker.resetTimeout.toMillis + 200)

    Get("/divide/10/2") ~> route ~> check {
      responseAs[String] shouldEqual "The result was 5"
    }
  }

}
