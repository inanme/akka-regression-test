package com.example

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }

import scala.concurrent.Future

class BasicDirectiveSpec
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

  val directive1: Directive1[Int] = provide(1)
  val directive2: Directive1[Int] = provide(2)
  val loggingDirective: Directive0 = Directive { next =>
    next(())
  }
  "Basic directives" should "be joined" in {
    val directive3: Directive[(Int, Int)] = directive1 & directive2
    Get() ~> directive3((m: Int, n: Int) => complete(m + n)) ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "be ORed1" in {
    val directive3: Directive1[Int] = directive1 | directive2
    Get() ~> directive3(r => complete(r)) ~> check {
      responseAs[Int] shouldBe 1
    }
  }
  it should "be ORed2" in {
    val directive3: Directive1[Int] = directive2 | directive1
    Get() ~> directive3(r => complete(r)) ~> check {
      responseAs[Int] shouldBe 2
    }
  }
  it should "be joined all" in {
    val directive3: Directive[(Int, Int)] = loggingDirective & directive2 & directive1
    Get() ~> directive3((m: Int, n: Int) => complete(m + n)) ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "be sequenced" in {
    val directive3: Directive1[Int] = directive1 flatMap { int1 =>
      directive2 flatMap { int2 =>
        provide(int1 + int2)
      }
    }
    Get() ~> directive3(r => complete(r)) ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "be sequenced1" in {
    val logs = loggingDirective {
      loggingDirective {
        loggingDirective {
          complete(StatusCodes.NoContent)
        }
      }
    }
    Get() ~> logs ~> check {
      response.status shouldBe StatusCodes.NoContent
    }
  }
  it should "be combined" in {
    val route1: Route = directive1 { int1 =>
      directive2 { int2 =>
        complete(int1 + int2)
      }
    }
    Get() ~> route1 ~> check {
      responseAs[Int] shouldBe 3
    }
  }
  it should "flatMap" in {
    val intParameter: Directive1[Int] = parameter("a".as[Int])

    //def tflatMap[R: Tuple](f: L => Directive[R]): Directive[R] =
    //def flatMap[R: Tuple](f: T => Directive[R]): Directive[R] =
    val myDirective: Directive1[Int] =
      intParameter.flatMap {
        case a if a > 0 => provide(2 * a)
        case _          => reject
      }

    // tests:
    Get("/?a=21") ~> myDirective(i => complete(i.toString)) ~> check {
      responseAs[String] shouldEqual "42"
    }
    Get("/?a=-18") ~> myDirective(i => complete(i.toString)) ~> check {
      handled shouldEqual false
    }
  }
  it should "tflatMap" in {
    val intParameter: Directive1[Int] = parameter("a".as[Int])

    //def tflatMap[R: Tuple](f: L => Directive[R]): Directive[R] =
    //def flatMap[R: Tuple](f: T => Directive[R]): Directive[R] =
    val myDirective: Directive1[Int] =
      intParameter.tflatMap {
        case Tuple1(a) if a > 0 => provide(2 * a)
        case _                  => reject
      }

    // tests:
    Get("/?a=21") ~> myDirective(i => complete(i.toString)) ~> check {
      responseAs[String] shouldEqual "42"
    }
    Get("/?a=-18") ~> myDirective(i => complete(i.toString)) ~> check {
      handled shouldEqual false
    }
  }
}
