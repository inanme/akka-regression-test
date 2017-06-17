package com.example

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.event.LoggingReceive
import akka.routing.{FromConfig, RandomGroup, RoundRobinGroup}

import scala.concurrent.Await
import scala.concurrent.duration._

object Messages {

  case object Start

  case object NewRequest

  case object Broadcast

  case class RandomRouterMessage(s: String)

}

object Child {
  def props = Props(new Child)
}

import com.example.Messages._

class Child extends Actor with ActorLogging {
  override def preStart(): Unit = {
    super.preStart()
    log.info("I am back")
  }

  override def receive: Receive = LoggingReceive {
    case Start =>
      log.info("I am ok!")
    case Broadcast =>
      log.info("broadcast")
    case NewRequest =>
      1 / 0
    case RandomRouterMessage(message) =>
      log.info(s"from router $message")
    case _ =>
      log.error("Unknown message!")
  }
}

object Parent {
  def props = Props(new Parent)
}

class Parent extends Actor with ActorLogging {
  var childs: Seq[ActorRef] = _
  var randomGroup: ActorRef = _
  var roundRobinGroup: ActorRef = _
  val supervisionStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
    case _: ArithmeticException => Restart
    case _ => Escalate
  }

  override def receive: Receive = LoggingReceive {
    case Start =>
      childs = Range(1, 4).map { it => context.actorOf(Child.props, "child" + it) }
      val paths = childs.map(_.path.toString).toList
      log.info(paths.toString)
      randomGroup = context.actorOf(RandomGroup(paths).props(), "my-random-router")
      roundRobinGroup = context.actorOf(RoundRobinGroup(paths).props(), "my-roundrobin-router")
    case NewRequest =>
      context.actorSelection("/user/parent/child*") ! Broadcast
      //context.child("child" + Random.nextInt(10)) foreach (it => it ! NewRequest)
      //      randomGroup ! NewRequest
      roundRobinGroup ! NewRequest

  }

  import context.dispatcher

  val timer = context.system.scheduler.schedule(2 second, 2 second, self, NewRequest)
}

object ParentChild extends App {
  val system = ActorSystem("PingPong2")

  val parent = system.actorOf(Parent.props, "parent")
  parent ! Start

  val router = system.actorOf(FromConfig.props(Child.props), "my-random-router")
  Range(1, 13).foreach { it =>
    router ! RandomRouterMessage(it.toString)
  }

  Await.ready(system.whenTerminated, Duration.Inf)
}
