package com.example

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import akka._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._

object BasicTransformation extends App with MyResources {
  val text = "Lorem Ipsum is"
  Source.fromIterator(() => text.split("\\s").iterator)
    .map(_.toUpperCase)
    .runForeach(println)
    .onComplete(_ => system.terminate())
}
object Tick extends App with MyResources {
  val in = Source.tick(1.second, 1.second, 1).limit(10)
  val in1 = Source.fromIterator(() => Iterator.iterate(0)(_ + 1).take(1000))

  def delayReturn[T]: T => T = t => {
    TimeUnit.MILLISECONDS.sleep(100)
    t
  }

  val double = Flow[Int].map(_ * 2)
  val sum = Flow[Int].fold(0)(_ + _)
  val sumV = Flow[Seq[Int]].fold[Int](0)((v, i) => delayReturn(i.sum + v))
  val buffer = Flow[Int].buffer(1000, OverflowStrategy.dropNew) // back-pressures the source if the buffer is full

  val out = Sink.foreach(println)
  val out1 = Sink.fold[Int, Int](0)(_ + _)
  val out2 = Sink.ignore
  in1.grouped(5).via(sumV).runWith(out).onComplete(_ => system.terminate())
}
//http://doc.akka.io/docs/akka/2.4.17/scala/stream/stream-flows-and-basics.html
//http://doc.akka.io/docs/akka/2.4.17/scala/stream/stream-graphs.html
object MyRunnableGraph extends App with MyResources {
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 1)
    val out = Sink.foreach(println)
    val branching = 2
    val bcast = builder.add(Broadcast[Int](branching))
    val merge = builder.add(Merge[Int](branching))

    def mkFlow(s: String) = {
      Flow[Int].map({ i =>
        val result = i + 10
        println(s + " " + result)
        result
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
object Tweets extends App with MyResources {
  final case class Author(handle: String)
  final case class Hashtag(name: String)
  final case class Tweet(author: Author, timestamp: Long, body: String) {
    def hashtags: Set[Hashtag] =
      body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
  }
  val akkaTag = Hashtag("#akka")
  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)
  tweets
    .filterNot(_.hashtags.contains(akkaTag))
    .mapConcat(_.hashtags)
    .map(_.name.toUpperCase)
    .runWith(Sink.foreach(println))
    .onComplete(_ => system.terminate())
}
object Kros extends App with MyResources {
  //in, out,mat
  val so: Source[Int, NotUsed] = Source(1 to 10)
  val fb: Flow[String, ByteString, NotUsed] = Flow[String].map(s => ByteString(s + "\n"))
  val fi: Flow[Int, Int, NotUsed] = Flow[Int].map(_ + 1)
  val s: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("X.confx"))
  //in, mat
  val x0: Sink[String, NotUsed] = fb.to(s) //to=toMat(sink)(Keep.left)
  val x1: Sink[String, NotUsed] = fb.toMat(s)(Keep.left)
  val x2: Sink[String, Future[IOResult]] = fb.toMat(s)(Keep.right)
  val x3: Sink[String, (NotUsed, Future[IOResult])] = fb.toMat(s)(Keep.both)
  val fs = so.map(_ + 1).map(_.toString).runWith(x2).onComplete(_ => system.terminate()) //toMat(sink)(Keep.right).run()
  val fx = so.via(fi).to(Sink.ignore) //via=viaMat(flow)(Keep.left)

  val source: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]

}
