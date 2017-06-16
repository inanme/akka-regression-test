package com.example

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class BasicTest1 extends TestKit(ActorSystem("Doubles-actior")) with WordSpecLike with Matchers with ImplicitSender {

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
      val d2 = system.actorOf(Doubler.props)

      within(300 milliseconds) {
        d2 ! 2
        expectMsg(4)
      }
    }
  }
}

