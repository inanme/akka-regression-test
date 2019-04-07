package com.example

import akka.actor._
import akka.util._
import akka.pattern._
import scala.concurrent._
import scala.concurrent.duration._

package fdfjdasflds23 {

  object MyRemoteApp1 extends App with MyRemoteResources1 {
    val actor = system.actorOf(Props[Echo], "echo")
    Await.result(system.whenTerminated, Duration.Inf)
  }
  object MyRemoteApp2 extends App with MyRemoteResources2 {
    implicit val timeout = Timeout(10 seconds)
    val actor = system.actorSelection("akka.tcp://remote1@127.0.0.1:2551/user/echo")
    sleep(1 second)
    (actor ? "hello").onComplete(printTry)
  }
}

