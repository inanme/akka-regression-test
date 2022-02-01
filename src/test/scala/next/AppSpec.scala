package next

import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.server.Directives
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
      responseAs[Signup] shouldBe Signup("???", "???")
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

}
