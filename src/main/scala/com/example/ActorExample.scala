package com.example

import java.util.concurrent.TimeUnit
import akka._
import akka.actor._
import akka.actor.Actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.event._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

package p394034i {
  object EvenOdd {
    case class Message(x: Int)
  }

  import EvenOdd._

  class EvenOdd extends Actor with ActorLogging with Stash {
    def receive = even

    def odd: Receive = LoggingReceive {
      case Message(curr) ⇒
        log.info(s"odd $curr")
        TimeUnit.MILLISECONDS.sleep(500L)
        context.become(even)
        self ! Message(curr + 1)
        unstashAll()
    }

    def even: Receive = LoggingReceive {
      case Message(curr) ⇒
        log.info(s"even $curr")
        TimeUnit.MILLISECONDS.sleep(500L)
        context.become(odd)
        self ! Message(curr + 1)
    }

  }
  object Main extends App with MyResources {
    val ref1 = system.actorOf(Props[EvenOdd])
    ref1 ! Message(0)
    Await.ready(system.whenTerminated, Duration.Inf)
  }
}

package bf3i29043 {
  case class Message(x: Int)
  class BufferingActor(printer: ActorRef) extends Actor with ActorLogging with Stash {
    val limit = 10
    var counter = 0

    override def receive = buffer

    def buffer: Receive = LoggingReceive {
      case m@Message(_) ⇒
        log.info(m.toString)
        counter += 1
        if (counter % 10 == 0) {
          context.become(send)
          unstashAll()
        } else {
          stash()
        }
      case x@_ ⇒
        log.info(s"buffer : what is this $x")
    }

    def send: Receive = LoggingReceive {
      case Done ⇒
        context.system.terminate()
      case x@_ ⇒
        log.info(s"send : what is this $x")
    }
  }
  class Printer extends Actor with ActorLogging {
    override def receive = ignoringBehavior
  }
  object Main extends App with MyResources {
    val bufferingActor = system.actorOf(Props(new Printer))
    val actorRef = system.actorOf(Props(new BufferingActor(bufferingActor)))
    val sink = Sink.actorRef(actorRef, Done)
    val stream = Source(1 to 10).map(Message).throttle(1, 1 second, 1, ThrottleMode.Shaping).runWith(sink)
    Await.ready(system.whenTerminated, Duration.Inf)

  }
}
