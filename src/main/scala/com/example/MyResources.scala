package com.example

import akka.actor.{ ActorSystem, Terminated }
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }

trait MyResources {
  implicit val system: ActorSystem                = ActorSystem("some-system")
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  def terminate(): Future[Terminated] =
    Await.ready(system.terminate(), 3 seconds)
}

trait MyRemoteResourcesReceiver {
  implicit val system: ActorSystem =
    ActorSystem("receiver", ConfigFactory.load().getConfig("receiver"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyRemoteResourcesSender {
  implicit val system: ActorSystem                = ActorSystem("sender", ConfigFactory.load().getConfig("sender"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyRemoteResources1 {
  implicit val system: ActorSystem                = ActorSystem("words", ConfigFactory.load().getConfig("node1"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyRemoteResources2 {
  implicit val system: ActorSystem                = ActorSystem("words", ConfigFactory.load().getConfig("node2"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyRemoteResources3 {
  implicit val system: ActorSystem                = ActorSystem("words", ConfigFactory.load().getConfig("node3"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyInmemResources {
  implicit val system: ActorSystem =
    ActorSystem("inmem", PersistenceConfig("akka.persistence.journal.inmem").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyFailingOnceResources {
  implicit val system: ActorSystem =
    ActorSystem("failing", PersistenceConfig("inmemFailOnceJournal").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

trait MyFailingResources {
  implicit val system: ActorSystem =
    ActorSystem("failing", PersistenceConfig("inmemTimingOutJournal").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
}

case class PersistenceConfig(
    journalPlugin: String,
    snapshotPlugin: Option[String] = None,
    journalDirectory: Option[String] = None,
    snapshotDirectory: Option[String] = None,
    redeliverInterval: Duration = 11 seconds,
    maxUnconfirmedMessages: Int = 30
) {
  def akkaConfig: Config = ConfigFactory.parseString(s"""akka {
       | test.filter-leeway = 20s
       |
       | log-config-on-start = off
       | loggers = ["akka.event.slf4j.Slf4jLogger"]
       | loglevel = DEBUG
       | logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
       |
       | debug {
       |  receive = on
       |  autoreceive = on
       |  fsm = on
       |  lifecycle = on
       |  unhandled = false
       |  event-stream = on
       |  router-misconfiguration = on
       | }
       |
       | persistence {
       |  journal-plugin-fallback.circuit-breaker.call-timeout = 100 millis
       |  at-least-once-delivery {
       |   redeliver-interval = $redeliverInterval
       |   max-unconfirmed-messages = $maxUnconfirmedMessages
       |   warn-after-number-of-unconfirmed-attempts : 3
       |  }
       |
       |  journal {
       |   plugin = "$journalPlugin"
       |   leveldb.dir = "${journalDirectory.getOrElse("")}"
       |  }
       |
       |  snapshot-store {
       |   plugin12 = "akka.persistence.snapshot-store.local"
       |   plugin="${snapshotPlugin.getOrElse("")}"
       |   local.dir = "${snapshotDirectory.getOrElse("")}"
       |  }
       | }
       |
       |}
       |
       |inmemTimingOutJournal {
       | class = "com.example.InmemTimingOutJournal"
       | plugin-dispatcher = "akka.actor.default-dispatcher"
       |}
       |
       |inmemFailOnceJournal{
       | class = "com.example.InmemFailOnceJournal"
       | plugin-dispatcher = "akka.actor.default-dispatcher"
       |}
       |
       | """.stripMargin)
}
