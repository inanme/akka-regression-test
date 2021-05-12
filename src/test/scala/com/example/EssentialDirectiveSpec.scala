package com.example

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }

import scala.concurrent.Future

class EssentialDirectiveSpec
    extends org.scalatest.flatspec.AnyFlatSpec
    with org.scalatest.matchers.should.Matchers
    with ScalatestRouteTest {

  import akka.http.scaladsl.model.MediaTypes._

  implicit final def unmarshaller: FromEntityUnmarshaller[Int] =
    Unmarshaller.stringUnmarshaller
      .flatMap(_ => _ => int => Future.successful(int.toInt))

  implicit final def marshaller: ToEntityMarshaller[Int] =
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, a.toString)
    }

  class Setup {
    val intParameter: Directive1[Int] = parameter("a".as[Int])
    val myDirective: Directive1[Int] =
      intParameter.tflatMap {
        case Tuple1(a) if a > 0 => provide(2 * a)
        case _                  => reject
      }

    val addheader: Directive0 = mapResponseHeaders(headers => headers :+ RawHeader("x", "y"))

    val route: Route = addheader {
      myDirective { i =>
        complete(i.toString)
      }
    }

    val routeHandling: Route = addheader {
      handleRejections(RejectionHandler.default) {
        myDirective { i =>
          complete(i.toString)
        }
      }
    }
  }

  "mapResponseHeaders" should "work" in new Setup {
    Get("/?a=21") ~> Route.seal(route) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldEqual "42"
      header("x").get.value() shouldBe "y"
    }
  }

  it should "not work" in new Setup {
    Get("/?a=-18") ~> Route.seal(route) ~> check {
      status shouldBe StatusCodes.NotFound
      header("x").get.value() shouldBe "y"
    }
  }

  it should "also work" in new Setup {
    Get("/?a=-18") ~> Route.seal(routeHandling) ~> check {
      status shouldBe StatusCodes.NotFound
      header("x").get.value() shouldBe "y"
    }
  }
}
