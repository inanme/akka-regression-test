package com.example

import akka.actor.ActorSystem


object ActorExample {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem.create("file-reader")
    val scanner = system.actorOf(FolderScannerActor.props, "scanner")
    val directoryPath = getClass.getResource("/dictionaries").getPath

    scanner ! directoryPath

    system.awaitTermination()
  }
}