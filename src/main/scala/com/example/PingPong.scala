package com.example

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class Ping(cnt: Int)
case class Pong(cnt: Int)
case object EndOfGame

class PingPong extends Actor {
  def terminate(n: Int) = n > 9

  def receive = {
    case Ping(counter) =>
      if (terminate(counter)) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter + " ping " + self.toString())
        sender ! Pong(counter + 1)
      }
    case Pong(counter) =>
      if (terminate(counter)) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter + " pong " + self.toString())
        sender ! Ping(counter + 1)
      }
    case EndOfGame =>
      context.system.terminate()
  }
}

object PingPongTest extends App {
  val system = ActorSystem("PingPongSystem")
  val player1 = system.actorOf(Props[PingPong], name = "player1")
  val player2 = system.actorOf(Props[PingPong], name = "player2")
  player2.tell(Ping(0), player1)
  Await.ready(system.whenTerminated, Duration.Inf)
}
