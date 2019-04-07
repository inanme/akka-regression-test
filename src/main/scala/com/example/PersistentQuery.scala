package com.example

import akka._
import akka.persistence.query._
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.scaladsl._
import scala.concurrent._
import scala.util._

package pr354890 {

  import com.example.event._

  object Main extends App with MyResources {
    val queries: Source[EventEnvelope, NotUsed] = PersistenceQuery(system)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
      .eventsByPersistenceId("101")
    // Grab a list of all the persistenceIds as of this moment
    val events = queries
      .take(30)
      .map(_.event)
      .map {
        case Added(value) ⇒ s"Added $value"
        case Subtracted(value) ⇒ s"Subtracted $value"
        case State(value) ⇒ s"State $value"
      }
    val matValue: Future[Done] = events.runWith(Sink.foreach(println))
    matValue.onComplete {
      case Success(_) =>
        Console println "Query completed successfully"
        system terminate ()

      case Failure(e) =>
        Console println e
        system terminate ()
    }
  }
  object Main2 extends App with MyResources {
    val queries = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    val nonInvasiveLog = (x: String) => {
      println(s"currentPersistenceId : $x")
      x
    }
    // Grab a list of all the persistenceIds as of this moment
    val events = queries.currentPersistenceIds()
      .map(nonInvasiveLog)
      // currentEventsByPersistenceId will grab all the events as of this moment for a supplied persistence id
      .flatMapConcat(eachPersistentId => queries.currentEventsByPersistenceId(eachPersistentId))
      .map(eventEnvelope => eventEnvelope.event)
      .map({
        case Added(value) => s"Added $value"
        case Subtracted(value) => s"Subtracted $value"
      })
    val matValue: Future[Done] = events.runWith(Sink.foreach(println))
    matValue.onComplete { it ⇒
      println(s"result $it")
      system.terminate()
    }
  }
}