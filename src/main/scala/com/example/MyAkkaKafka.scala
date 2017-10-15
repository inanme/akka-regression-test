package com.example

import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{IntegerDeserializer, IntegerSerializer, StringDeserializer, StringSerializer}
import scala.concurrent.Future
import scala.language.postfixOps

object MyAkkaKafka extends MyResources {
  val bootstrapServers = "localhost:9093"
  //bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 3 --topic a-topic
  //bin/kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic a-topic --from-beginning

  val topicName = "a-topic"

  def integerProducerSettings = ProducerSettings(system, new IntegerSerializer, new IntegerSerializer)
    .withBootstrapServers(bootstrapServers)

  val integerConsumerSettings: ConsumerSettings[Integer, Integer] =
    ConsumerSettings(system, new IntegerDeserializer, new IntegerDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val stringProducerSettings: ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)
  val stringConsumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

}

import MyAkkaKafka._

object MyAkkaKafkaProducer extends App {
  Source(1 to 10)
    .map { elem => new ProducerRecord[String, String](topicName, (elem % 3).toString, elem.toString) }
    .runWith(Producer.plainSink(stringProducerSettings))
}
object MyAkkaKafkaConsumer extends App {
  val consumerSettings = stringConsumerSettings.withGroupId("MyAkkaKafkaConsumer")
  Consumer.committableSource(consumerSettings, Subscriptions.topics(topicName))
    .mapAsync(3) { msg ⇒
      val message = msg.record
      val offset = msg.committableOffset.partitionOffset
      println(message)
      println(offset)
      Future.successful(msg)
    }
    .mapAsync(3) { msg ⇒
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)
}
object MyAkkaKafkaConsumerExt extends App {
  val consumerSettings = stringConsumerSettings.withGroupId("MyAkkaKafkaConsumerExt")
  val topics = Map(
    new TopicPartition(topicName, 0) -> 0L,
    new TopicPartition(topicName, 1) -> 0L,
    new TopicPartition(topicName, 2) -> 0L)
  val control: Consumer.Control = Consumer.plainSource(consumerSettings, Subscriptions.assignmentWithOffset(topics))
    .mapAsync(3) { msg =>
      println(msg)
      Future.successful(msg)
    }
    .toMat(Sink.ignore)(Keep.left)
    .run()
}
object MyAkkaKafkaDoubler extends App {
  val consumerSettings = integerConsumerSettings.withGroupId("integer")
  val step1 = "step-1"
  val step2 = "step-2"
  Source(1 to 6)
    .map { i ⇒ new ProducerRecord[Integer, Integer](step1, i) }
    .runWith(Producer.plainSink(integerProducerSettings))
  Consumer.committableSource(consumerSettings, Subscriptions.topics(step1))
    .map { msg ⇒
      println(s"$step1 -> $step2: $msg")
      ProducerMessage.Message(new ProducerRecord[Integer, Integer](step2, msg.record.value * 3), msg.committableOffset)
    }
    .runWith(Producer.commitableSink(integerProducerSettings))
  val control: Consumer.Control = Consumer.committableSource(consumerSettings, Subscriptions.topics(step2))
    .mapAsync(3) { msg ⇒
      println(msg)
      Future.successful(msg)
    }
    .toMat(Sink.ignore)(Keep.left)
    .run()
}
