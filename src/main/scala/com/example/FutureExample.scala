package com.example

import java.util.concurrent.TimeUnit

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random
import scala.util.{Failure, Success}

object FutureExample extends App {
  type CoffeeBeans = String
  type GroundCoffee = String
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  case class Water(temperature: Int) extends AnyVal

  case class GrindingException(msg: String) extends Exception(msg)

  case class FrothingException(msg: String) extends Exception(msg)

  case class WaterBoilingException(msg: String) extends Exception(msg)

  case class BrewingException(msg: String) extends Exception(msg)

  def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
    println("start grinding...")
    Thread.sleep(Random.nextInt(2000))
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = Future {
    println("heating the water now")
    Thread.sleep(Random.nextInt(2000))
    println("hot, it's hot!")
    Water(85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    Thread.sleep(Random.nextInt(2000))
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's brewed!")
    "espresso"
  }

  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

  def temperatureOkay(water: Water): Future[Boolean] = Future {
    water.temperature > 100
  }

  //  val acceptable: Future[Boolean] = for {
  //    heatedWater <- heatWater(Water(25))
  //    okay <- temperatureOkay(heatedWater)
  //  } yield okay
  //
  //  acceptable.onComplete {
  //    case Success(result) => println(s"acceptable is $result")
  //    case Failure(ex) => println("acceptable is " + ex.getMessage)
  //  }

  val acceptable1: Future[Cappuccino] = {
    val groundCoffee = grind("arabica beans")
    val heatedWater = heatWater(Water(20))
    val frothedMilk = frothMilk("milk")
    for {
      ground <- groundCoffee
      water <- heatedWater
      foam <- frothedMilk
      espresso <- brew(ground, water)
    } yield combine(espresso, foam)
  }

  grind("baked beans").onComplete {
    case Success(ground) => println(s"got my $ground")
    case Failure(ex) => println("This grinder needs a replacement, seriously! " + ex.getMessage)
  }

  TimeUnit.SECONDS.sleep(3)
  Await.ready(acceptable1, Duration.Inf)

}

object PromiseExample extends App {

  case class TaxCut(message: String)

  case class LameExcuse(excuse: String) extends RuntimeException(excuse, null, false, false)

  def redeemCampaignPledge(): Future[TaxCut] = {
    val p = Promise[TaxCut]()
    Future {
      println("Starting the new legislative period.")
      Thread.sleep(2000)
      p.failure(LameExcuse("global economy crisis"))
      println("We didn't fulfill our promises, but surely they'll understand.")
    }
    p.future
  }

  redeemCampaignPledge().onComplete({
    case Success(TaxCut(message)) => println(message)
    case Failure(ex) => ex.printStackTrace()
  })

  TimeUnit.SECONDS.sleep(3)

}