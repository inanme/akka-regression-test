package com.example

import akka._
import akka.actor._
import akka.persistence.AtLeastOnceDelivery._
import akka.persistence._
import akka.stream.scaladsl._
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._

package r43ejdsmkl {
  case object MyInit
  case object MyAck
  @SerialVersionUID(1L)
  case class Print(deliveryId: Long, message: String)
  case class OK(deliveryId: Long)
  case object Snapshot
  object MyAtLeastOnceDelivery extends App with MyResources {
    val printer = system.actorOf(Props[GoodPrinter])
    val props = Props(new MyAtLeastOnceDelivery(printer))
    val myActor = system.actorOf(props)
    Source(1 to 10)
      .map(it ⇒ "message" + it)
      .runWith(
        Sink.actorRefWithAck(
          ref = myActor,
          onInitMessage = MyInit,
          ackMessage = MyAck,
          onCompleteMessage = Done
        ))
    //    (1 to 10).foreach(it ⇒ {
    //      myActor ! "message" + it
    //      TimeUnit.SECONDS.sleep(2L)
    //    })
    Await.result(system.whenTerminated, Duration.Inf)
  }
  class GoodPrinter extends Actor with ActorLogging {
    override def receive: Receive = {
      case Print(deliveryId, message) ⇒
        log.info(s"Printing : $message")
        sender() ! OK(deliveryId)
    }
  }
  class BadPrinter extends Actor with ActorLogging {
    override def receive: Receive = {
      case Print(deliveryId, message) ⇒
        if (deliveryId % 5 != 0) {
          log.info(s"Printing : $message")
          sender() ! OK(deliveryId)
        } else {
          log.info(s"Fault : $message")
        }
    }
  }
  class MyAtLeastOnceDelivery(printer: ActorRef) extends AtLeastOnceDelivery with ActorLogging {
    override def persistenceId = "12447389"

    import context.dispatcher

    var cancellable: Cancellable = _

    override def preStart(): Unit = {
      super.preStart()
      cancellable = context.system.scheduler.schedule(3 seconds, 3 seconds, self, Snapshot)
    }

    override def postStop(): Unit = {
      super.postStop()
      swallow(cancellable.cancel())
    }

    override def receiveRecover: Receive = {
      case message: String ⇒
        deliver(printer.path) { deliveryId ⇒
          Print(deliveryId, message)
        }
      case SnapshotOffer(metadata, snapshot) ⇒
        val adjustedUnconfirmedDeliveries =
          snapshot.asInstanceOf[AtLeastOnceDeliverySnapshot]
            .getUnconfirmedDeliveries
            .asScala
            .map(_.copy(destination = printer.path))
        log.debug("Recovering from Snapshot: {} with {} unconfirmed deliveries", metadata, adjustedUnconfirmedDeliveries.size)
        setDeliverySnapshot(snapshot.asInstanceOf[AtLeastOnceDeliverySnapshot].copy(unconfirmedDeliveries = adjustedUnconfirmedDeliveries.toList))
      case it ⇒ log.info(s"<<receiveRecover>> : $it")
    }

    var upstream: ActorRef = _

    override def receiveCommand: Receive = {
      case Done ⇒ context.system.terminate().onComplete(printTry)
      case MyInit ⇒
        upstream = sender()
        upstream ! MyAck
      case message: String ⇒
        persist(message) { _ ⇒
          deliver(printer.path) { deliveryId ⇒
            Print(deliveryId, message)
          }
        }
      case OK(deliveryId) ⇒
        swallow(confirmDelivery(deliveryId))
        upstream ! MyAck
      case Snapshot ⇒
        val x = getDeliverySnapshot
        log.debug("Saving snapshot {}", x)
        saveSnapshot(x)
      case SaveSnapshotSuccess(metadata) ⇒
        log.debug(s"SaveSnapshotSuccess metadata")
        deleteMessages(metadata.sequenceNr)
        deleteSnapshots(SnapshotSelectionCriteria(maxTimestamp = metadata.timestamp - 1))
      //      case UnconfirmedWarning(unconfirmedDeliveries) ⇒
      //        unconfirmedDeliveries.foreach(it ⇒ {
      //          log.debug(s"Manual confirm of ${it.message}")
      //          confirmDelivery(it.deliveryId)
      //        })
      case it ⇒ log.info(s"<<receiveCommand>>: $it")
    }
  }
}
