package com.example

import akka.actor.typed._
import akka.actor.typed.scaladsl._

package fjdsaklfjdaslkfusdiokl {

  object PrintMyActorRefActor {
    def apply(): Behavior[String] =
      Behaviors.setup { context =>
        Behaviors.receiveMessage {
          case "printit" =>
            val secondRef = context.spawn(Behaviors.empty[String], "second-actor")
            println(s"Second: $secondRef")
            Behaviors.same
        }
      }
  }

  object Main extends App {
    val b = Behaviors.setup[String] { context =>
      Behaviors.receiveMessage[String] {
        case "start" =>
          val firstRef = context.spawn(PrintMyActorRefActor(), "first-actor")
          println(s"First: $firstRef")
          firstRef ! "printit"
          Behaviors.same
      }
    }
    val m: ActorSystem[String] = ActorSystem(b, "testSystem")
    m ! "start"
  }

}

package jfdklafjkldsfdsjkgfdl {

  object StartStopActor1 {
    def apply(): Behavior[String] =
      Behaviors.setup(new StartStopActor1(_))
  }

  class StartStopActor1(context: ActorContext[String]) extends AbstractBehavior[String](context) {
    println("first started")
    context.spawn(StartStopActor2(), "second")

    override def onMessage(msg: String): Behavior[String] =
      msg match {
        case "stop" => Behaviors.stopped
      }

    override def onSignal: PartialFunction[Signal, Behavior[String]] = {
      case PostStop =>
        println("first stopped")
        this
    }

  }

  object StartStopActor2 {
    def apply(): Behavior[String] =
      Behaviors.setup(new StartStopActor2(_))
  }

  class StartStopActor2(context: ActorContext[String]) extends AbstractBehavior[String](context) {
    println("second started")

    override def onMessage(msg: String): Behavior[String] = Behaviors.unhandled

    override def onSignal: PartialFunction[Signal, Behavior[String]] = {
      case PostStop =>
        println("second stopped")
        this
    }
  }

  object Main extends App {
    val m: ActorSystem[String] = ActorSystem(StartStopActor1(), "testSystem")
    m ! "stop"
  }

}

package fdjsklfjdsklnfdmbtriocxkla {

  object SupervisingActor {
    def apply(): Behavior[String] =
      Behaviors.setup(new SupervisingActor(_))
  }

  class SupervisingActor(context: ActorContext[String]) extends AbstractBehavior[String](context) {
    private val child = context.spawn(
      Behaviors.supervise(SupervisedActor()).onFailure(SupervisorStrategy.restart),
      name = "supervised-actor")

    override def onMessage(msg: String): Behavior[String] =
      msg match {
        case "failChild" =>
          child ! "fail"
          this
      }
  }

  object SupervisedActor {
    def apply(): Behavior[String] =
      Behaviors.setup(new SupervisedActor(_))
  }

  class SupervisedActor(context: ActorContext[String]) extends AbstractBehavior[String](context) {
    println("supervised actor started")

    override def onMessage(msg: String): Behavior[String] =
      msg match {
        case "fail" =>
          println("supervised actor fails now")
          throw new Exception("I failed!")
      }

    override def onSignal: PartialFunction[Signal, Behavior[String]] = {
      case PreRestart =>
        println("supervised actor will be restarted")
        this
      case PostStop =>
        println("supervised actor stopped")
        this
    }

  }

  object Main extends App {
    val m: ActorSystem[String] = ActorSystem(SupervisingActor(), "testSystem")
    m ! "failChild"
  }
}
