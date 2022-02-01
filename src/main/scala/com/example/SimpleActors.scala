package com.example

import akka.actor.Actor._
import akka.actor._
import akka.event.LoggingReceive

object Doubler {
  def props = Props(new Doubler())
}

class Doubler extends Actor with ActorLogging {
  override def receive: Receive =
    LoggingReceive {
      case x: Int =>
        sender() ! x * 2
    }
}

class Echo extends Actor with ActorLogging {
  override def receive: Receive = {
    case it => sender() ! it
  }
}

class HelloWorld extends Actor with ActorLogging {
  override def receive: Receive = {
    case it: String => sender() ! s"Hello $it"
  }
}

class SimpleSender extends Actor {
  override def receive: Receive = ignoringBehavior
}
