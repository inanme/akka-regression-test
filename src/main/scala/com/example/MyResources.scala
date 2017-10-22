package com.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

trait MyResources {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyRemoteResources1 {
  implicit val system: ActorSystem = ActorSystem("remote1", ConfigFactory.load().getConfig("remote1"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyRemoteResources2 {
  implicit val system: ActorSystem = ActorSystem("remote1", ConfigFactory.load().getConfig("remote2"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyCluster {
  implicit val system: ActorSystem = ActorSystem("words", ConfigFactory.load().getConfig("cluxter"))
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyInmemResources {
  implicit val system: ActorSystem = ActorSystem("inmem", PersistenceConfig("akka.persistence.journal.inmem").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyFailingOnceResources {
  implicit val system: ActorSystem = ActorSystem("failing", PersistenceConfig("inmemFailOnceJournal").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
trait MyFailingResources {
  implicit val system: ActorSystem = ActorSystem("failing", PersistenceConfig("inmemTimingOutJournal").akkaConfig)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
case class PersistenceConfig(journalPlugin: String, snapshotPlugin: Option[String] = None,
                             journalDirectory: Option[String] = None, snapshotDirectory: Option[String] = None,
                             redeliverInterval: Duration = 11 seconds, maxUnconfirmedMessages: Int = 30) {
  def akkaConfig: Config = ConfigFactory.parseString(
    s"""akka {
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
       | class = "org.inanme.InmemTimingOutJournal"
       | plugin-dispatcher = "akka.actor.default-dispatcher"
       |}
       |
       |inmemFailOnceJournal{
       | class = "org.inanme.InmemFailOnceJournal"
       | plugin-dispatcher = "akka.actor.default-dispatcher"
       |}
       |
       | """.stripMargin
  )
}
