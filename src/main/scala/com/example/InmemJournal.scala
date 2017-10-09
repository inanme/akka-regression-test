package org.inanme

import java.util.concurrent.TimeUnit
import scala.collection.immutable
import scala.concurrent.Future
import scala.util.Try
import akka.persistence.journal.AsyncWriteJournal
import akka.persistence.PersistentRepr
import akka.persistence.AtomicWrite

/*

akka {
  persistence {
    journal.plugin = "pg-journal"
  }
}

pg-journal {
  # class name of the jdbc journal plugin
  class = "org.inanme.InmemJournal"
  plugin-dispatcher = "akka.actor.default-dispatcher"
}

 */

class InmemJournal extends AsyncWriteJournal {
  override def asyncWriteMessages(messages: immutable.Seq[AtomicWrite]): Future[immutable.Seq[Try[Unit]]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      TimeUnit.SECONDS.sleep(1000L)
      Nil
    }
  }

  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    Future.successful(1L)
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long, max: Long)
                                  (recoveryCallback: PersistentRepr ⇒ Unit): Future[Unit] = {
    Future.successful(())
  }

  def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    Future.successful(())
  }
}
