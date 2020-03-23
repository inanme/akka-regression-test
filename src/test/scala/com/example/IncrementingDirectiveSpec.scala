package com.example

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ FreeSpec, Matchers }

class IncrementingDirectiveSpec extends FreeSpec with Matchers with ScalatestRouteTest {

  "The Increment Directive" - {
    "will increment just once" in {
      val counter = new AtomicLong()

      def inc: Directive1[Long] = provide(counter.incrementAndGet())

      val route: Route = extractRequest { _ =>
        inc { value =>
          complete(value.toString)
        }
      }
      val val1 = Get() ~> route ~> check {
        responseAs[String]
      }
      val val2 = Get() ~> route ~> check {
        responseAs[String]
      }
      val1 shouldNot equal(val2)
    }
  }

}