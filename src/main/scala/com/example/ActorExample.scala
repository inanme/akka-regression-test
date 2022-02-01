package com.example

import akka._
import akka.actor._
import akka.event._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration._

package p394034i {

  object EvenOdd {
    case class Message(x: Int)
    def props: Props = Props(new EvenOdd)
  }

  import com.example.p394034i.EvenOdd._

  class EvenOdd extends Actor with ActorLogging {
    def receive: Receive = even
    def hasSender()      = sender() != context.system.deadLetters
    def odd: Receive =
      LoggingReceive {
        case Message(curr) =>
          log.info(s"odd $curr")
          sleep(500L millis)
          context.become(even)
          self ! Message(curr + 1)
      }

    def even: Receive =
      LoggingReceive {
        case Message(curr) =>
          log.info(s"even $curr")
          sleep(500L millis)
          context.become(odd)
          self ! Message(curr + 1)
      }

  }

  object Main extends App with MyResources {
    val ref1 = system.actorOf(EvenOdd.props, "even-odd")
    ref1.tell(Message(0), ActorRef.noSender)
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}

package bf3i29043 {

  case class Message(x: Int)

  object BufferingActor {
    def props: Props = Props(new BufferingActor)
  }

  class BufferingActor extends Actor with ActorLogging with Stash {

    override def receive: Receive = buffer(0)

    def buffer(counter: Int): Receive =
      LoggingReceive {
        case Done =>
          context.become(send)
          stash()
          unstashAll()
        case Message(_) =>
          context.become(buffer(counter + 1))
          stash()
      }

    def send: Receive =
      LoggingReceive {
        case Done =>
          val _ = context.system.terminate()
        case Message(_) =>
      }
  }

  object Main extends App with MyResources {
    val actorRef = system.actorOf(BufferingActor.props)

    val sink = Sink.actorRef(actorRef, Done, _ => Done)
    val stream =
      Source(1 to 10).map(Message).throttle(1, 1 second, 1, ThrottleMode.Shaping).runWith(sink)
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
