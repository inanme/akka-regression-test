package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicLong

class MetricDirectiveSpec extends AnyFreeSpec with Matchers with ScalatestRouteTest {

  "The Metric Directive" - {
    "will increment just once" in {
      val counter = new AtomicLong()
      //type Route = RequestContext => Future[RouteResult]
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
      //type Route = RequestContext => Future[RouteResult]
      val metric1: Directive0 = Directive { inner => ctx =>
        inner {
          val _ = counter.incrementAndGet()
          ()
        }(ctx)
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
      val counter             = new AtomicLong()
      val metric1: Directive0 = extract(_ => counter.incrementAndGet()).flatMap(_ => pass)
      val route: Route        = metric1 & complete(String.valueOf(counter.get()))
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
