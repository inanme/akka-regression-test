package com.example

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ FreeSpec, Matchers }

class MetricDirectiveSpec extends FreeSpec with Matchers with ScalatestRouteTest {

  "The Metric Directive" - {
    "will increment just once" in {
      val counter = new AtomicLong()
      //next = T => Route
      val metric1: Directive0 = Directive { next =>
        counter.incrementAndGet()
        next(())
      }
      val route: Route = metric1 & complete(String.valueOf(counter.get()))
      val val1 = Get() ~> route ~> check {
        responseAs[String]
      }
      val val2 = Get() ~> route ~> check {
        responseAs[String]
      }
      val1 should equal(val2)
    }

    "will NOT increment just once" in {
      val counter = new AtomicLong()
      val metric1: Directive0 = Directive { inner => ctx =>
        //        val l1: Unit => Route = inner
        //        val l2: RequestContext = ctx
        inner({
          val _ = counter.incrementAndGet()
        })(ctx)
      }
      val route: Route = metric1 & complete(String.valueOf(counter.get()))
      val val1 = Get() ~> route ~> check {
        responseAs[String]
      }
      val val2 = Get() ~> route ~> check {
        responseAs[String]
      }
      val1 shouldNot equal(val2)
    }
    "will NOT increment just once either" in {
      val counter = new AtomicLong()
      val metric1: Directive0 = extract(_ => counter.incrementAndGet()).flatMap(_ => pass)
      val route: Route = metric1 & complete(String.valueOf(counter.get()))
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