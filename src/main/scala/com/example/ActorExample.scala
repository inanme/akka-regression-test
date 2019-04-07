package com.example

import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.event._
import scala.concurrent.Await
import scala.concurrent.duration._

package p394034i {

  object EvenOdd {

    case class Message(x: Int)

    def props: Props = Props(new EvenOdd)
  }

  import EvenOdd._

  class EvenOdd extends Actor with ActorLogging {
    def receive: Receive = even

    def odd: Receive = LoggingReceive {
      case Message(curr) ⇒
        log.info(s"odd $curr")
        sleep(500L millis)
        context.become(even)
        self ! Message(curr + 1)
    }

    def even: Receive = LoggingReceive {
      case Message(curr) ⇒
        log.info(s"even $curr")
        sleep(500L millis)
        context.become(odd)
        self ! Message(curr + 1)
    }

  }

  object Main extends App with MyResources {
    val ref1 = system.actorOf(EvenOdd.props, "even-odd")
    val simpleSender = system.actorOf(Props[SimpleSender], "simple-sender")
    ref1.tell(Message(0), simpleSender)
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}

package bf3i29043 {

  case class Message(x: Int)

  object BufferingActor {
    def props: Props = Props(new BufferingActor)
  }

  class BufferingActor extends Actor with ActorLogging with Stash {

    import context.dispatcher

    val limit = 10
    var counter = 0

    override def receive: Receive = buffer

    def buffer: Receive = LoggingReceive {
      case Message(_) ⇒
        log.info(s"who is sender $sender")
        counter += 1
        if (counter % 10 == 0) {
          context.become(send)
          unstashAll()
        } else {
          stash()
        }
    }

    def send: Receive = LoggingReceive {
      case Done ⇒
        context.system.terminate().onComplete(printTry)
      case x ⇒
        log.info(s"send : what is this $x")
    }
  }

  object Main extends App with MyResources {
    val actorRef = system.actorOf(BufferingActor.props)
    val sink = Sink.actorRef(actorRef, Done)
    val stream = Source(1 to 10).map(Message).throttle(1, 1 second, 1, ThrottleMode.Shaping).runWith(sink)
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
