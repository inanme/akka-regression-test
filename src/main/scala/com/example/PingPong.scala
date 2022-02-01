package com.example

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PingPong {
  case class Ping(cnt: Int)
  case class Pong(cnt: Int)
  case object EndOfGame
}

import com.example.PingPong._

class PingPong extends Actor {

  import context.dispatcher

  def terminate(n: Int): Boolean = n > 9

  def receive: Actor.Receive = {
    case Ping(counter) =>
      if (terminate(counter)) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter.toString + " ping " + self.toString())
        sender() ! Pong(counter + 1)
      }
    case Pong(counter) =>
      if (terminate(counter)) {
        sender() ! EndOfGame
        self ! PoisonPill
      } else {
        println(counter.toString + " pong " + self.toString())
        sender() ! Ping(counter + 1)
      }
    case EndOfGame =>
      context.system.terminate().onComplete(printTry)
  }
}
object PingPongTest extends App with MyResources {
  val player1 = system.actorOf(Props[PingPong](), name = "player1")
  val player2 = system.actorOf(Props[PingPong](), name = "player2")
  player2.tell(Ping(0), player1)
  Await.ready(system.whenTerminated, Duration.Inf)
}
