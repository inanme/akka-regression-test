package com.example

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class BasicFutureTest extends TestKit(ActorSystem("Doubles-actor")) with WordSpecLike with Matchers {
  implicit val timeout: Timeout = 100 milliseconds
  implicit val ec: ExecutionContext = system.dispatcher
  "Doubler" should {
    "double" in {
      val d2 = system.actorOf(Doubler.props)
      val result = d2 ? 5
      result map {
        case x: Future[Int@unchecked] => x should equal(10)
        case _ => it should fail
      }
    }
  }
}
