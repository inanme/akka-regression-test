package com.example

import akka.persistence.journal.AsyncWriteJournal
import akka.persistence.{ AtomicWrite, PersistentRepr }

import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.immutable
import scala.concurrent.Future
import scala.util.Try

abstract class JournalBase extends AsyncWriteJournal {
  override def asyncReadHighestSequenceNr(
      persistenceId: String,
      fromSequenceNr: Long
  ): Future[Long] = Future.successful(1L)

  override def asyncReplayMessages(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long,
      max: Long
  )(recoveryCallback: PersistentRepr => Unit): Future[Unit] = Future.successful(())

  def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] =
    Future.successful(())
}
class InmemTimingOutJournal extends JournalBase {
  override def asyncWriteMessages(
      messages: immutable.Seq[AtomicWrite]
  ): Future[immutable.Seq[Try[Unit]]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      sleepForever()
      Nil
    }
  }
}
class InmemFailOnceJournal extends JournalBase {
  val flag = new AtomicBoolean(false)

  override def asyncWriteMessages(
      messages: immutable.Seq[AtomicWrite]
  ): Future[immutable.Seq[Try[Unit]]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future {
      if (!flag.getAndSet(true))
        sleepForever()
      Nil
    }
  }
}
