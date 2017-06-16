package com.example

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive

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
