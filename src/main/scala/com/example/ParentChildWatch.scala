package com.example

import akka.actor.SupervisorStrategy._
import akka.actor._
import akka.event.LoggingReceive
import akka.routing.RandomGroup

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object Messages {
  case object Start
  case class NewRequest(i: Int)
  case object Next
  case class Message(s: String)
}

import com.example.Messages._

object Child {
  def props = Props(new Child)
}
class Child extends Actor with ActorLogging {
  override def preStart(): Unit = {
    super.preStart()
    log.info("I am alive")
  }
  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    log.error("postRestart : {}", reason.getMessage)
  }
  override def receive: Receive = LoggingReceive {
    case NewRequest(x) =>
      log.info(s"new message $x")
      1 / 0
    //      log.info("bye")
    //      self ! PoisonPill
    case Message(message) =>
      log.info(s"message $message")
    case _ =>
      log.error("Unknown message!")
  }
}

object Parent {
  def props = Props(new Parent)
}

class Parent extends Actor with ActorLogging {
  var counter = 0
  var curr: ActorRef = _
  var liveChildren : Set[String] = _

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException ⇒ Stop
      case _: NullPointerException ⇒ Restart
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception ⇒ Escalate
    }

  override def receive: Receive = LoggingReceive {
    case Start ⇒
      val children = Range(1, 4).map(it ⇒ context.actorOf(Child.props, "child" + it))
      children.foreach(context.watch)
      liveChildren = children.map(_.path.toString).toSet
      setCurr()
    case Next ⇒
      //context.actorSelection("/user/parent/child*") ! Broadcast  //1
      //context.child("child" + Random.nextInt(10)) foreach (it => it ! NewRequest) //2
      curr ! NewRequest(counter) //3
      counter += 1
    case Terminated(actor) ⇒
      log.info("{} died", actor.path.toString)
      curr ! PoisonPill
      liveChildren -= actor.path.toString
      if (liveChildren.isEmpty) {
        context.system.terminate()
      } else {
        setCurr()
      }
    case m@_ ⇒ log.info("watch out " +m)
  }

  def setCurr(): Unit = {
    //roundRobinGroup = context.actorOf(RoundRobinGroup(paths).props(), "my-roundrobin-router")
    curr = context.actorOf(RandomGroup(liveChildren).props(), s"my-random-router.${System.currentTimeMillis()}")
  }

  import context.dispatcher

  val timerHandle: Cancellable = context.system.scheduler.schedule(
    initialDelay = 2 second,
    interval = 2 second,
    receiver = self,
    message = Next)
}

object ParentChild extends App {
  val system = ActorSystem("ParentChild")
  val parent = system.actorOf(Parent.props, "parent")
  parent ! Start

  //  val router = system.actorOf(FromConfig.props(Child.props), "my-random-router")
  //  Range(1, 13).foreach { it =>
  //    router ! RandomRouterMessage(it.toString)
  //  }

  Await.ready(system.whenTerminated, Duration.Inf)
}
