package com.example

import akka.actor._
import akka.event._
import akka.persistence._
import scala.concurrent._
import scala.concurrent.duration._

package a38409324 {
  object MyPersistentActor {
    def props = Props(new MyPersistentActor)
  }

  import command._, event._

  class MyPersistentActor extends PersistentActor with ActorLogging {
    override val persistenceId: String = "101"
    var state = State()

    def update(op: Event): Unit = op match {
      case Added(n) =>
        state = State(state.value + n)
      case Subtracted(n) =>
        state = State(state.value - n)
      case _ =>
        log.info(s"ignoring $op")
    }

    override def receiveRecover: Receive = LoggingReceive {
      case e: Event =>
        update(e)
        log.info(s"recovery mode ${state.value}")
      case SnapshotOffer(_, snapshot: State) =>
        log.info(s"received snapshot $snapshot")
        state = snapshot
      case it =>
        log.info(s"what is this $it")
    }

    override def receiveCommand: Receive = LoggingReceive {
      case Print =>
        log.info(s"State is ${state.value}")
      case c@Add(x) =>
        log.info(s"received $c")
        val event = Added(x)
        persist(event) { evt =>
          update(event)
          if (Math.floorMod(state.value, 3) == 0) {
            saveSnapshot(state)
          }
        }
      case c@Subtract(x) =>
        log.info(s"received $c")
        val event = Subtracted(x)
        persist(event) { evt =>
          update(event)
          if (Math.floorMod(state.value, 3) == 0) {
            saveSnapshot(state)
          }
        }

      case it =>
        log.info(s"what is this $it")
    }

  }
  object Main extends App with MyResources {
    val myActor = system.actorOf(MyPersistentActor.props, "MyPersistentActor")
    Range(1, 5).foreach { _ =>
      myActor ! Add(1)
    }
    myActor ! Print
    Await.result(system.whenTerminated, Duration.Inf)
  }
}