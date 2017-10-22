package com

import com.typesafe.scalalogging.Logger
import scala.util.{Failure, Success, Try}

package object example {
  val logger = Logger("com.example")
  val printTry: PartialFunction[Try[_], Unit] = {
    case a@Success(_) ⇒ logger.info(a.toString)
    case Failure(e) ⇒ logger.error("Try", e)
  }
  val swallow: PartialFunction[Any, Unit] = {
    case _ ⇒
  }
}
