package com.example

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.duration._

class BasicTest extends TestKit(ActorSystem("Doubles-actor")) with WordSpecLike with Matchers {

  "Doubler" should {
    "double" in {
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
