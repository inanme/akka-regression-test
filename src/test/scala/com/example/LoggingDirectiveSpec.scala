package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.EventFilter

class LoggingDirectiveSpec
    extends org.scalatest.freespec.AnyFreeSpec
    with org.scalatest.matchers.should.Matchers
    with ScalatestRouteTest {
  override def testConfigSource: String =
    s"""akka {
       | test.filter-leeway = 3s
       | loggers = ["akka.testkit.TestEventListener", "akka.event.slf4j.Slf4jLogger"]
       | }
       | """.stripMargin

  "Loggers" - {
    "should log" in {
      case class SomeException() extends RuntimeException
      val route: Route = extractLog { log =>
        log.error("joe")
        complete("ok")
      }
      EventFilter.error(message = "joe", occurrences = 1) intercept {
        Get() ~> route ~> check {
          responseAs[String] shouldBe "ok"
        }
      }
    }
  }
}
