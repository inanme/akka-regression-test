package com.example

import java.io.File

import akka.actor.ActorSystem
import akka.pattern.BackoffSupervisor

import scala.concurrent.Await
import scala.concurrent.duration._

object ActorExample {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem.create("file-reader")
    //val scanner = system.actorOf(FolderScannerActor.props, "scanner")
    Duration
    val supervisor = BackoffSupervisor.props(
      FolderScannerActor.props,
      childName = "myEcho",
      minBackoff = 1.seconds,
      maxBackoff = 3.seconds,
      randomFactor = 0.2) // adds 20% "noise" to vary the intervals slightly

    val scanner = system.actorOf(supervisor, name = "scanner")
    val directoryPath: String = getClass.getResource("/dictionaries").getPath

    scanner ! new File(directoryPath)

    Await.ready(system.whenTerminated, Duration.Inf)
  }
}
