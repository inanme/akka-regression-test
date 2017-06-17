package com.example

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.example.MyPersistentActor.{Cmd, Decr, Incr}

import scala.concurrent.Await
import scala.concurrent.duration._

object MyPersistentActor {
  def props = Props(new MyPersistentActor)

  sealed trait Operation {
    def count: Int
  }

  case class Incr(count: Int) extends Operation

  case class Decr(count: Int) extends Operation

  case class Cmd(op: Operation)

  case class Evt(op: Operation)

  case class State(count: Int)

}

class MyPersistentActor extends PersistentActor with ActorLogging {

  import MyPersistentActor._

  var state = State(count = 0)

  def update(op: Operation): Unit = op match {
    case Incr(n) =>
      state = State(state.count + n)
    case Decr(n) =>
      state = State(state.count - n)
  }

  override def receiveRecover: Receive = LoggingReceive {
    case Evt(op) =>
      update(op)
      log.info(s"recovery mode ${state.count}")
    case SnapshotOffer(_, snapshot: State) =>
      log.info("received snaphot")
      state = snapshot
  }

  override def receiveCommand: Receive = LoggingReceive {
    case Cmd(op) =>
      log.info(s"receive $op")
      persist(Evt(op)) { evt =>
        update(evt.op)
      }
  }

  override val persistenceId: String = MyPersistentActor.getClass.getName
}

object MyPersistenceActorApp extends App {
  val system = ActorSystem("MyPersistenceActorApp")
  val myactor = system.actorOf(MyPersistentActor.props, "MyPersistentActor")
  myactor ! Cmd(Incr(1))
  myactor ! Cmd(Incr(1))
  myactor ! Cmd(Decr(1))
  Await.result(system.whenTerminated, Duration.Inf)
}
