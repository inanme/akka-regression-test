package com.example

import java.io.File

import akka.actor.{Actor, PoisonPill, Props}
import akka.event.Logging

import scala.collection.mutable.ListBuffer
import scala.io.Source

class FileReaderActor extends Actor {

  val log = Logging.getLogger(context.system, this)

  def receive = {
    case f: File => {

      log.info(s"Reading file ${f.getName}")
      var words = new ListBuffer[String]
      Source.fromFile(f).getLines().foreach(line => words += line )

      sender() ! words.toList
      self ! PoisonPill
    }
    case _ => log.info("Still waiting for a text file")

  }
}

object FileReaderActor {
  def props = Props(new FileReaderActor)
}
