package com.example

import java.io.File

import akka.actor.{Actor, Props}
import akka.event.Logging

import scala.collection.mutable.ListBuffer

case object DoneWriting

class FolderScannerActor extends Actor {

  val log = Logging.getLogger(context.system, this)

  var filesNumber = 0
  var responsesNumber = 0
  var words = new ListBuffer[String]

  def listFiles(folder: File): List[File] = {
    if (folder.exists && folder.isDirectory) {
      folder.listFiles.toList
    } else {
      List.empty
    }
  }

  def receive = {
    case folder: File => {
      log.info(s"Scanning ${folder.getAbsoluteFile}")
      val files = listFiles(folder)
      filesNumber = files.size
      files.foreach(file => context.actorOf(FileReaderActor.props) ! file)
    }
    case wordsList: List[String@unchecked] => {
      log.info(s"New words are received ${wordsList}")
      responsesNumber += 1
      words insertAll(words.size, wordsList)
      if (filesNumber == responsesNumber) {
        context.actorOf(FileWriterActor.props) ! words.toList
      }
    }
    case DoneWriting => {
      log.info("shutting down")
      context.system.terminate()
    }
    case _ => log.info("Nothing to scan...")
  }

}

object FolderScannerActor {
  def props = Props(new FolderScannerActor)
}
