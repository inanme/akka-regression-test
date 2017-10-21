package com.example

import java.util.concurrent._
import akka.actor._
import akka.persistence._
import akka.persistence.AtLeastOnceDelivery._
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConverters._

package r43ejdsmkl {
  case class Print(deliveryId: Long, message: String)
  case class OK(deliveryId: Long)
  case object Snapshot
  object MyAtLeastOnceDelivery extends App with MyResources {
    val printer = system.actorOf(Props[Printer])
    val props = Props(new MyAtLeastOnceDelivery(printer))
    val myActor = system.actorOf(props)
    (1 to 10).foreach(it ⇒ {
      myActor ! "message" + it
      TimeUnit.SECONDS.sleep(2L)
    })
    Await.result(system.whenTerminated, Duration.Inf)

  }
  class Printer extends Actor with ActorLogging {
    override def receive = {
      case Print(deliveryId, message) ⇒
        log.info(s"Printing : $message")
        if (deliveryId % 5 != 0) {
          sender() ! OK(deliveryId)
        }
    }
  }
  class MyAtLeastOnceDelivery(printer: ActorRef) extends AtLeastOnceDelivery with ActorLogging {
    override def persistenceId = "12447389"

    import context.dispatcher

    var cancellable: Cancellable = _

    override def preStart(): Unit = {
      super.preStart()
      cancellable = context.system.scheduler.schedule(1 second, 1 second, self, Snapshot)
    }

    override def receiveRecover = {
      case SnapshotOffer(metadata, snapshot) =>
        val adjustedUnconfirmedDeliveries: Seq[AtLeastOnceDelivery.UnconfirmedDelivery] = snapshot.asInstanceOf[AtLeastOnceDeliverySnapshot]
          .getUnconfirmedDeliveries.asScala
          .map(_.copy(destination = printer.path))
        log.debug("Recovering from Snapshot: {} with {} unconfirmed deliveries", metadata, adjustedUnconfirmedDeliveries.size)
        setDeliverySnapshot(snapshot.asInstanceOf[AtLeastOnceDeliverySnapshot].copy(unconfirmedDeliveries = adjustedUnconfirmedDeliveries.toList))

      case it ⇒ log.info(s"What is this : $it")
    }

    override def receiveCommand = {
      case it: String ⇒
        persist(it) { _ ⇒
          deliver(printer.path) { deliveryId =>
            Print(deliveryId, it)
          }
        }
      case OK(deliveryId) ⇒
        swallow(confirmDelivery(deliveryId))

      case Snapshot =>
        val x: AtLeastOnceDelivery.AtLeastOnceDeliverySnapshot = getDeliverySnapshot
        log.debug("Saving snapshot {}", x)
        saveSnapshot(x)

      case SaveSnapshotSuccess(metadata) =>
        log.debug(s"SaveSnapshotSuccess metadata")
        deleteMessages(metadata.sequenceNr)
        deleteSnapshots(SnapshotSelectionCriteria(maxTimestamp = metadata.timestamp - 1))
    }

  }
}
