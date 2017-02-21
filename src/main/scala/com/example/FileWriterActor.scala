package com.example

import akka.actor.{PoisonPill, Props, Actor}
import akka.event.Logging

class FileWriterActor extends Actor {
  val log = Logging.getLogger(context.system, this)

  def receive = {
    case words: List[x] => {
      println(s"doing fake write ${words}")
      sender() ! DoneWriting()
      self ! PoisonPill
    }
  }
}

object FileWriterActor {
  def props = Props(new FileWriterActor)
}
