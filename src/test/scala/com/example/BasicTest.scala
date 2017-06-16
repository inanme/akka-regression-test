package com.example

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class BasicTest extends TestKit(ActorSystem("Doubles-actor")) with WordSpecLike with Matchers {

  object Doubler {
    def props = Props(new Doubler())
  }

  class Doubler extends Actor with ActorLogging {
    override def receive: Receive = LoggingReceive {
      case x: Int => {
        sender ! x * 2
      }
    }
  }

  "x" should {
    "y" in {
      val probe1 = TestProbe()
      val d2 = system.actorOf(Doubler.props)

      within(300 milliseconds) {
        probe1.send(d2, 2)
        val result = probe1.expectMsgType[Int]
        result should equal(4)
      }

    }
  }
}

