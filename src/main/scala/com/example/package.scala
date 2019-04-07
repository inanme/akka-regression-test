package com

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.FiniteDuration
import scala.util._

package object example {
  val logger = Logger("com.example")
  val printTry: PartialFunction[Try[_], Unit] = {
    case a @ Success(_) ⇒ logger.info(a.toString)
    case Failure(e) ⇒ logger.error("Try", e)
  }
  val swallow: PartialFunction[Any, Unit] = {
    case _ ⇒
  }

  def sleep(duration: FiniteDuration) = TimeUnit.MILLISECONDS.sleep(duration.toMillis)
  def sleepForever(): Unit = TimeUnit.DAYS.sleep(Long.MaxValue)
}
