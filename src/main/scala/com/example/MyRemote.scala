package com.example

import akka.actor._
import akka.util._
import akka.pattern._
import scala.concurrent._
import scala.concurrent.duration._

package fdfjdasflds23 {

  object MyRemoteApp1 extends App with MyRemoteResourcesReceiver {
    val actor = system.actorOf(Props[HelloWorld](), "echo")
    Await.result(system.whenTerminated, Duration.Inf)
  }
  object MyRemoteApp2 extends App with MyRemoteResourcesSender {
    implicit val timeout = Timeout(10 seconds)
    val actor            = system.actorSelection("akka://receiver@host01:2551/user/echo")
    sleep(1 second)
    (actor ? "joe").onComplete(printTry)
  }
}
