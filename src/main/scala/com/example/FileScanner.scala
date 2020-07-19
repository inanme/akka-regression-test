package com.example

import java.io.File

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.{ BackoffOpts, BackoffSupervisor }
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

object FileScanner extends App with MyResources {
  val appName = "scanner"
  val config  = ConfigFactory.load().getConfig(appName)
  //val scanner = system.actorOf(FolderScannerActor.props, "scanner")
  val supervisor = BackoffSupervisor.props(
    BackoffOpts.onStop(
      FolderScannerActor.props,
      childName = "myEcho",
      minBackoff = 1 seconds,
      maxBackoff = 3 seconds,
      randomFactor = 0.2
    )
  ) // adds 20% "noise" to vary the intervals slightly

  val scanner = system.actorOf(supervisor, name = "scanner")
  val directoryPath: String =
    getClass.getResource(config.getString("file-reader.directoryPath")).getPath
  scanner ! new File(directoryPath)
  Await.ready(system.whenTerminated, Duration.Inf)
}

case object DoneWriting

object FileWriterActor {
  def props = Props(new FileWriterActor)
}

class FileWriterActor extends Actor with ActorLogging {
  def receive =
    LoggingReceive {
      case words: List[_] =>
        println(s"doing fake write $words")
        sender() ! DoneWriting
        self ! PoisonPill
    }
}

object FileReaderActor {
  def props = Props(new FileReaderActor)
}

class FileReaderActor extends Actor with ActorLogging {
  val random = new Random()

  def receive =
    LoggingReceive {
      case f: File =>
        log.info(s"Reading file ${f.getName}")
        if (random.nextBoolean()) {
          sender() ! Source.fromFile(f).getLines().toList
          self ! PoisonPill
        } else {
          val zero = 1 - 1
          println(1 / (1 - zero))
        }
      case _ => log.info("Still waiting for a text file")
    }
}

object FolderScannerActor {
  def props = Props(new FolderScannerActor)
}

class FolderScannerActor extends Actor with ActorLogging {

  import context.dispatcher

  var filesNumber     = 0
  var responsesNumber = 0
  var words           = new ListBuffer[String]

  def listFiles(folder: File): List[File] =
    if (folder.exists && folder.isDirectory)
      folder.listFiles.toList
    else
      List.empty

  def receive =
    LoggingReceive {
      case folder: File =>
        log.info(s"Scanning ${folder.getAbsoluteFile}")
        val files = listFiles(folder)
        filesNumber = files.size
        files.foreach(file => context.actorOf(FileReaderActor.props) ! file)
      case wordsList: List[String @unchecked] =>
        log.info(s"New words are received $wordsList")
        responsesNumber += 1
        words.insertAll(words.size, wordsList)
        if (filesNumber == responsesNumber)
          context.actorOf(FileWriterActor.props) ! words.toList
      case DoneWriting =>
        log.info("shutting down")
        context.system.terminate().onComplete(printTry)
      case _ => log.info("Nothing to scan...")
    }
}
