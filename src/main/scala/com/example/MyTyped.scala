package com.example

import akka.actor.typed._
import akka.actor.typed.scaladsl._

package fjdsaklfjdaslkfusdiokl {

  object PrintMyActorRefActor {
    def apply(): Behavior[String] =
      Behaviors.setup(context => new PrintMyActorRefActor(context))
  }

  class PrintMyActorRefActor(context: ActorContext[String]) extends AbstractBehavior[String] {

    override def onMessage(msg: String): Behavior[String] =
      msg match {
        case "printit" =>
          val secondRef = context.spawn(Behaviors.empty[String], "second-actor")
          println(s"Second: $secondRef")
          this
      }
  }

  object ThisIsNotMain {
    def apply(): Behavior[String] =
      Behaviors.setup(context => new ThisIsNotMain(context))

  }

  class ThisIsNotMain(context: ActorContext[String]) extends AbstractBehavior[String] {
    override def onMessage(msg: String): Behavior[String] =
      msg match {
        case "start" =>
          val firstRef = context.spawn(PrintMyActorRefActor(), "first-actor")
          println(s"First: $firstRef")
          firstRef ! "printit"
          this
      }
  }

  object Main extends App {
    val m: ActorSystem[String] = ActorSystem(ThisIsNotMain(), "testSystem")
    m ! "start"
  }

}

package jfdklafjkldsfdsjkgfdl {

  object StartStopActor1 {
    def apply(): Behavior[String] =
      Behaviors.setup(context => new StartStopActor1(context))
  }

  class StartStopActor1(context: ActorContext[String]) extends AbstractBehavior[String] {
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
      Behaviors.setup(_ => new StartStopActor2)
  }

  class StartStopActor2 extends AbstractBehavior[String] {
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
      Behaviors.setup(context => new SupervisingActor(context))
  }

  class SupervisingActor(context: ActorContext[String]) extends AbstractBehavior[String] {
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
      Behaviors.setup(_ => new SupervisedActor)
  }

  class SupervisedActor extends AbstractBehavior[String] {
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