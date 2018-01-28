package com.example

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.Future

class BasicDirectiveSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  import akka.http.scaladsl.model.MediaTypes._

  implicit final def unmarshaller: FromEntityUnmarshaller[Int] =
    Unmarshaller.stringUnmarshaller
      .flatMap(ctx => mat => int => Future.successful(int.toInt))

  implicit final def marshaller: ToEntityMarshaller[Int] =
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, a.toString)
    }

  val directive1: Directive1[Int] = provide(1)
  val directive2: Directive1[Int] = provide(2)
  "Basic directives" should "be compose" in {
    val directive3: Directive[(Int, Int)] = directive1 & directive2
    Get() ~> directive3 { (m: Int, n: Int) => complete(m + n) } ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "be sequenced" in {
    val directive3 = directive1 flatMap { int1 ⇒
      directive2 flatMap { int2 ⇒
        provide(int1 + int2)
      }
    }
    Get() ~> directive3 { r => complete(r) } ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "be combined" in {
    val route1: Route = directive1 { int1 ⇒
      directive2 { int2 ⇒
        complete(int1 + int2)
      }
    }
    Get() ~> route1 ~> check {
      responseAs[Int] shouldBe 3
    }
  }
}