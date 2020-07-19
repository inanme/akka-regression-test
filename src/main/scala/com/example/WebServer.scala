package com.example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow

package fjdksla349 {
  object Handler2 extends App with MyResources {
    val handler: HttpRequest => HttpResponse = _ => HttpResponse(status = StatusCodes.BadRequest)
    Http().bindAndHandle(Flow.fromFunction(handler), "localhost", 8080)
  }
  object Route132 extends App with MyResources {
    val route =
      path("hello" / Segment) { name =>
        get {
          complete(s"hello $name")
        }
      }
    Http().bindAndHandle(route, "localhost", 8080)
  }
  object Route13 extends App with MyResources {
    val route =
      path("hello" / Segment) { _ =>
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
    Http().bindAndHandle(route, "localhost", 8080)

  }
}
