package com.example

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object AkkaBecome {
  case class Message(x: Int)
}

import com.example.AkkaBecome._

object Actor1 {
  def props = Props(new Actor1)
}

class Actor1 extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case Message(curr) ⇒
      context.become(newReceive(curr + 1))
      self ! Message(curr + 1)
  }

  def newReceive(x: Int): Actor.Receive = new Actor.Receive {
    override def isDefinedAt(x: Any): Boolean = true
    override def apply(v1: Any): Unit = {
      log.info(s"x is $x")
    }
  }
}

object ActorExample extends App {
  val appName = "scanner"
  val system = ActorSystem.create(appName)
  val ref1 = system.actorOf(Actor1.props)

  ref1 ! Message(0)

  Await.ready(system.whenTerminated, Duration.Inf)
}

