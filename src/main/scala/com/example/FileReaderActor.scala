package com.example

import java.io.File

import akka.actor.{Actor, PoisonPill, Props}
import akka.event.Logging

import scala.io.Source
import scala.util.Random

class FileReaderActor extends Actor {

  val log = Logging.getLogger(context.system, this)

  val random = new Random()

  def receive = {
    case f: File => {
      log.info(s"Reading file ${f.getName}")
      if (random.nextBoolean()) {
        sender() ! Source.fromFile(f).getLines().toList
        self ! PoisonPill
      } else {
        val x = 1 / 0
      }
    }
    case _ => log.info("Still waiting for a text file")

  }
}

object FileReaderActor {
  def props = Props(new FileReaderActor)
}
