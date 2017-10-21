package com.example

import akka.actor._
import akka.testkit._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

class EmptyTest extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  import system.dispatcher

  override def afterAll() = {
    system.terminate().onComplete(printTry)
  }

}
