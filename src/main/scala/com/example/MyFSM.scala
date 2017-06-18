package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, FSM, Props}
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration._

object MyFSM {

  final case class SetTarget(ref: ActorRef)

  final case class Queue(obj: Any)

  case object Flush

  final case class Batch(obj: Seq[Any])

  sealed trait State

  case object Idle extends State

  case object Active extends State

  sealed trait Data

  case object Uninitialized extends Data

  final case class Todo(target: ActorRef, queue: Seq[Any]) extends Data

  def props = Props(new MyFSM)
}

import com.example.MyFSM._

class MyFSM extends FSM[State, Data] {

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(SetTarget(ref), Uninitialized) =>
      stay using Todo(ref, Vector.empty)
  }

  onTransition {
    case Active -> Idle =>
      stateData match {
        case Todo(ref, queue) =>
          log.info(ref.path.toString)
          ref ! Batch(queue)
        case _ => // nothing to do
      }
  }

  when(Active, stateTimeout = 5 seconds) {
    case Event(Flush | StateTimeout, t: Todo) =>
      log.info("timeout")
      goto(Idle) using t.copy(queue = Vector.empty)
  }

  whenUnhandled {
    // common code for both states
    case Event(Queue(obj), t@Todo(_, v)) =>
      goto(Active) using t.copy(queue = v :+ obj)

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()
}

object MyFSMPrinter {
  def props = Props(new MyFSMPrinter)
}

class MyFSMPrinter extends Actor with ActorLogging {
  override def receive: Receive = LoggingReceive {
    case it =>
      log.info(it.toString)
  }
}

object MyFSMApp extends App {
  val system = ActorSystem()
  val fsm = system.actorOf(MyFSM.props, "fsm")
  val printer = system.actorOf(MyFSMPrinter.props, "printer")

  fsm ! SetTarget(printer)
  fsm ! Queue(1)
  fsm ! Queue(2)
  fsm ! Queue(3)
  fsm ! Flush

  Await.ready(system.whenTerminated, Duration.Inf)
}