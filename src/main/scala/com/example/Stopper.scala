package com.example

import akka.actor._
import akka.pattern._
import scala.concurrent._
import scala.concurrent.duration._

package fdjsiofsdjkld {

  case object StopTheActor

  case object FailTheActor

  class SomeActor extends Actor {
    override def receive: Receive = {
      case StopTheActor => context.stop(self)
      case FailTheActor => throw new RuntimeException
      case x: Int       => println(x)
    }
  }

  object Main extends App with MyResources {

    val protectedActorProps = BackoffSupervisor.props(
      BackoffOpts.onStop(
        Props[SomeActor](),
        childName = "MyPersistentActor",
        minBackoff = 10 millis,
        maxBackoff = 100 millis,
        randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      )
    )

    val protectedActor = system.actorOf(protectedActorProps, "protected-actor")
    val simpleActor    = system.actorOf(Props[SomeActor](), "simple-actor")

    (1 to 10).foreach { it =>
      protectedActor ! it
      if (it == 4) protectedActor ! StopTheActor
      sleep(1 second)
    }

    sleep(30 seconds)
    Await.result(system.terminate(), Duration.Inf)
  }

}
