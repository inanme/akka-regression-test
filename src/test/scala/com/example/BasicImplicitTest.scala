package com.example

import akka.actor._
import akka.testkit._
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class BasicImplicitTest
    extends TestKit(ActorSystem("Doubles-actor"))
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with Eventually {

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
