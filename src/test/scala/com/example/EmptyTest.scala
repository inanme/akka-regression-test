package com.example

import akka.actor._
import akka.testkit._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class EmptyTest
    extends TestKit(ActorSystem("MySpec"))
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll() =
    system.terminate().onComplete(printTry)

  "sample test" in {
    val echo = system.actorOf(Props[Echo]())
    echo ! 123
    echo ! "mert"
    expectMsg(123)
    expectMsg("mert")
  }

}
