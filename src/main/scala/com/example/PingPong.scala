package com.example

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case object PingMessage

case object PongMessage

case object EndOfGame

class PingPong extends Actor {
  var counter: Int = _

  def terminate: Boolean = {
    counter += 1
    counter > 9
  }

  def receive = {
    case PingMessage =>
      if (terminate) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter + " ping " + self.toString())
        sender ! PongMessage
      }
    case PongMessage =>
      if (terminate) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter + " pong " + self.toString())
        sender ! PingMessage
      }
    case EndOfGame =>
      context.system.terminate()
  }
}

object PingPongTest extends App {
  val system = ActorSystem("PingPongSystem")
  val player1 = system.actorOf(Props[PingPong], name = "player1")
  val player2 = system.actorOf(Props[PingPong], name = "player2")
  player2.tell(PingMessage, player1)
  Await.ready(system.whenTerminated, Duration.Inf)
}