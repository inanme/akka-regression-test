package next

import cats.data.NonEmptyList
import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.semiauto._
import pureconfig.module.cats._

object AppConfig {

  case class FruitBasket(apples: NonEmptyList[Int])

  case object FruitBasket {
    implicit def configReader: ConfigReader[FruitBasket] = deriveReader[FruitBasket]
  }

  val p1: Result[FruitBasket] = ConfigSource
    .string("{apples: [1,2,3]}")
    .load[FruitBasket]

}
