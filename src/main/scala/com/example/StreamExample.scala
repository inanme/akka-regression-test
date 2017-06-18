package com.example

import java.util.concurrent._

import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.duration._

object BasicTransformation extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val text =
    """|Lorem Ipsum is simply dummy text of the printing and typesetting industry.
       |Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,
       |when an unknown printer took a galley of type and scrambled it to make a type
       |specimen book.""".stripMargin

  Source.fromIterator(() => text.split("\\s").iterator)
    .map(_.toUpperCase)
    .runForeach(println)
    .onComplete(_ => system.terminate())

  // could also use .runWith(Sink.foreach(println)) instead of .runForeach(println) above
  // as it is a shorthand for the same thing. Sinks may be constructed elsewhere and plugged
  // in like this. Note also that foreach returns a future (in either form) which may be
  // used to attach lifecycle events to, like here with the onComplete.
}

object Tick extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val in = Source.tick(1.second, 1.second, 1).limit(10)
  val in1 = Source.fromIterator(() => Iterator.iterate(0)(_ + 1).take(10000))

  def delayReturn[T]: T => T = t => {
    TimeUnit.MILLISECONDS.sleep(100)
    t
  }

  val double = Flow[Int].map(_ * 2)
  val str = Flow[Int].map[String](_.toString + " as str")
  val sum = Flow[Int].fold(0)(_ + _)
  val sumV = Flow[Seq[Int]].fold[Int](0)((v, i) => delayReturn(i.sum + v))
  val group = Flow[Int].grouped(2)
  val buffer = Flow[Int].buffer(1000, OverflowStrategy.dropNew) // back-pressures the source if the buffer is full

  val out = Sink.foreach(println)
  val out1 = Sink.fold[Int, Int](0)(_ + _)
  val out2 = Sink.ignore

  in1.via(double).via(group).via(sumV).via(buffer).runWith(out).onComplete(_ => system.terminate())
}

//http://doc.akka.io/docs/akka/2.4.17/scala/stream/stream-flows-and-basics.html
//http://doc.akka.io/docs/akka/2.4.17/scala/stream/stream-graphs.html
object MyRunnableGraph extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 1)
    val out = Sink.foreach(println)

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    def mkFlow(s: String) = {
      Flow[Int].map({ i =>
        println(s + " " + i)
        i + 10
      })
    }

    val f1 = mkFlow("f1")
    val f2 = mkFlow("f2")
    val f3 = mkFlow("f3")
    val f4 = mkFlow("f4")

    //@formatter:off
    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
                bcast ~> f4 ~> merge
    //@formatter:on
    ClosedShape
  }).run()
}
