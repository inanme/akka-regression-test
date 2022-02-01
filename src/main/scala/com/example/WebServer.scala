package com.example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow

package fjdksla349 {

  import akka.http.scaladsl.model.headers.RawHeader
  import akka.http.scaladsl.server.{ Directive0, Directive1, Route }

  object Handler2 extends App with MyResources {
    val handler: HttpRequest => HttpResponse = _ => HttpResponse(status = StatusCodes.BadRequest)
    Http().newServerAt("localhost", 8080).bindFlow(Flow.fromFunction(handler))
  }
  object Route132 extends App with MyResources {
    val route =
      path("hello" / Segment) { name =>
        get {
          complete(s"hello $name")
        }
      }
    Http().newServerAt("localhost", 8080).bind(route)
  }
  object Route13 extends App with MyResources {
    val route =
      path("hello" / Segment) { _ =>
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
    Http().newServerAt("localhost", 8080).bind(route)
  }

  object Route14 extends App with MyResources {
    val intParameter: Directive1[Int] = parameter("a".as[Int])
    val myDirective: Directive1[Int] =
      intParameter.tflatMap {
        case Tuple1(a) if a > 0 => provide(2 * a)
        case _                  => reject
      }

    val addheader: Directive0 = mapResponseHeaders(headers => headers :+ RawHeader("x", "y"))

    val route: Route = addheader {
      myDirective { i =>
        complete(i.toString)
      }
    }
    Http().newServerAt("localhost", 8080).bind(Route.seal(route))
  }
}
