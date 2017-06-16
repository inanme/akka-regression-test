package com.example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class BasicImplicitTest extends TestKit(ActorSystem("Doubles-actor"))
  with WordSpecLike with Matchers with ImplicitSender {

  "Doubler" should {
    "double the input" in {
      val d2 = system.actorOf(Doubler.props)
      within(300 milliseconds) {
        d2 ! 2
        expectMsg(4)
      }
    }
  }
}
