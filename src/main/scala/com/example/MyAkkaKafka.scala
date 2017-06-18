package com.example

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.Future

object MyAkkaKafka {
  val bootstrapServers = "localhost:9093"
  //bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 3 --topic a-topic
  val topicName = "a-topic"
}

import com.example.MyAkkaKafka._

object MyAkkaKafkaProducer extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  val done = Source(1 to 6)
    .map { elem => new ProducerRecord[String, String](topicName, (elem % 3).toString, elem.toString) }
    .runWith(Producer.plainSink(producerSettings))
}

object MyAkkaKafkaConsumer extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId("MyAkkaKafkaConsumer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName))
    .mapAsync(3) { msg =>
      val message = msg.record
      val offset = msg.committableOffset.partitionOffset
      println(message)
      println(offset)
      Future.successful(msg)
    }
    .mapAsync(3) { msg =>
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)
}


object MyAkkaKafkaConsumerExt extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId("MyAkkaKafkaConsumerExt")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val topics = Map(
    new TopicPartition(topicName, 0) -> 0L,
    new TopicPartition(topicName, 1) -> 0L,
    new TopicPartition(topicName, 2) -> 0L)
  Consumer.plainSource(consumerSettings, Subscriptions.assignmentWithOffset(topics))
    .mapAsync(3) { msg =>
      println(msg)
      Future.successful(msg)
    }
    .runWith(Sink.ignore)
}
