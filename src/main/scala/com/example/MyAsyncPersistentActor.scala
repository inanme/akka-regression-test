package com.example

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.example.MyPersistentActor._
import scala.concurrent.Await
import scala.concurrent.duration._

object MyAsyncPersistentActor extends App {
  def props = Props(new MyAsyncPersistentActor)

  val system = ActorSystem("MyPersistenceActorApp")
  val myactor = system.actorOf(props, "MyPersistentActor")
  (1 to 10).foreach { _ =>
    myactor ! Cmd(Incr(1))
  }
  myactor ! Cmd(Print)
  Await.result(system.whenTerminated, Duration.Inf)

}
class MyAsyncPersistentActor extends PersistentActor with ActorLogging {
  override val persistenceId: String = "KDS349"

  import MyPersistentActor._

  var state = State(count = 0)

  def update(op: Operation): Unit = op match {
    case Incr(n) =>
      state = State(state.count + n)
    case Decr(n) =>
      state = State(state.count - n)
    case _ =>
      log.info(s"ignoring $op")
  }

  override def receiveRecover: Receive = LoggingReceive {
    case Evt(op) =>
      log.info(s"recovery op $op")
      update(op)
      log.info(s"recovery mode1 ${state.count}")
    case it =>
      log.info(s"what is this1 $it")
  }

  override def receiveCommand: Receive = LoggingReceive {
    case Cmd(Print) =>
      log.info(s"State is ${state.count}")
    case Cmd(op@(Incr(_) | Decr(_))) =>
      log.info(s"receive $op")
      persistAsync(Evt(op)) { evt =>
        update(evt.op)
      }
    case it =>
      log.info(s"what is this2 $it")
  }

  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.error(cause, event.toString)
  }
}

