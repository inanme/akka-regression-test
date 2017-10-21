package com

import scala.util.{Failure, Success, Try}

package object example {
  val printTry: PartialFunction[Try[_], Unit] = {
    case Success(_) ⇒
    case Failure(e) ⇒ e.printStackTrace()
  }
  val swallow: PartialFunction[Any, Unit] = {
    case _ ⇒
  }
}
