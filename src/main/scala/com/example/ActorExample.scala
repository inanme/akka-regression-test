package com.example

import java.io.File

import akka.actor.ActorSystem

object ActorExample {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem.create("file-reader")
    val scanner = system.actorOf(FolderScannerActor.props, "scanner")
    val directoryPath: String = getClass.getResource("/dictionaries").getPath

    scanner ! new File(directoryPath)

    system.awaitTermination()
  }
}
