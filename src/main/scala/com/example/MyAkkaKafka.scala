package com.example

import akka.actor.{Actor, ActorLogging, Props}

object MyAkkaKafka {
  def props = Props(new MyAkkaKafka)
}

class MyAkkaKafka extends Actor with ActorLogging {
  override def receive: Receive = ???
}
