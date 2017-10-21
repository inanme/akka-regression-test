package com.example

import akka.actor._
import akka.event._
import akka.persistence._
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

package a38409324 {
  object MyPersistentActor {
    def props = Props(new MyPersistentActor)
  }

  import command._, event._

  class MyPersistentActor extends PersistentActor with ActorLogging {
    override val persistenceId: String = "101"
    var state = State()

    def update(op: Event): Unit = op match {
      case Added(n) ⇒
        state = State(state.value + n)
      case Subtracted(n) ⇒
        state = State(state.value - n)
      case _ ⇒
        log.info(s"ignoring $op")
    }

    override def receiveRecover: Receive = LoggingReceive {
      case e: Event ⇒
        update(e)
        log.info(s"recovery mode ${state.value}")
      case SnapshotOffer(_, snapshot: State) ⇒
        log.info(s"received snapshot $snapshot")
        state = snapshot
      case it ⇒
        log.info(s"what is this $it")
    }

    override def receiveCommand: Receive = LoggingReceive {
      case Print ⇒
        log.info(s"State is ${state.value}")
      case Add(x) ⇒
        persist(Added(x)) { event ⇒
          update(event)
          if (Math.floorMod(state.value, 3) == 0) {
            saveSnapshot(state)
          }
        }
      case Subtract(x) ⇒
        persist(Subtracted(x)) { event ⇒
          update(event)
          if (Math.floorMod(state.value, 3) == 0) {
            saveSnapshot(state)
          }
        }
      case SaveSnapshotSuccess(metadata) =>
        log.debug(s"SaveSnapshotSuccess metadata $metadata")
        deleteMessages(metadata.sequenceNr)
        deleteSnapshots(SnapshotSelectionCriteria(maxTimestamp = metadata.timestamp - 1))

      case it ⇒
        log.info(s"what is this $it")
    }

  }
  object Persistence extends App with MyResources {
    val myActor = system.actorOf(MyPersistentActor.props, "MyPersistentActor")
    Range(1, 11).foreach { i ⇒
      myActor ! Add(i)
    }
    myActor ! Print
    TimeUnit.SECONDS.sleep(3L)
    Await.result(system.terminate(), Duration.Inf)
  }
}

package jdfsakl4372804dfsf {

  import akka.pattern.{Backoff, BackoffSupervisor}
  import com.typesafe.scalalogging.Logger

  class MyPersistentActor extends PersistentActor with ActorLogging {
    override def receiveRecover = Actor.emptyBehavior

    override def receiveCommand = {
      case x: Int ⇒ persist(x)(print)
    }

    override def persistenceId = "48390fasdfdsa"
  }
  object Persistence extends App with MyFailingResources {
    val logger = Logger[Persistence]

    val actor = BackoffSupervisor.props(
      Backoff.onStop(
        Props[MyPersistentActor],
        childName = "MyPersistentActor",
        minBackoff = 100 millis,
        maxBackoff = 100 millis,
        randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      ))
    val myActor = system.actorOf(actor, "MyPersistentActor")
    Range(1, 11).foreach { i ⇒
      myActor ! i
      TimeUnit.MILLISECONDS.sleep(110L)
    }
    TimeUnit.SECONDS.sleep(10L)
    Await.result(system.terminate(), Duration.Inf)
  }
}
