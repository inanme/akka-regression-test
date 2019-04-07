package com.example

import akka.actor._
import akka.testkit._
import org.scalatest._

class EmptyTest extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll() = {
    system.terminate().onComplete(printTry)
  }

  "sample test" in {
    val echo = system.actorOf(Props[Echo])
    echo ! 123
    echo ! "mert"
    expectMsg(123)
    expectMsg("mert")
  }

}
