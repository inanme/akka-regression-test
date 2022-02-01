package next

import akka.http.scaladsl.server.{ Directive1, Directives }
import io.circe.generic.JsonCodec

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@JsonCodec
case class Signup(email: String, password: String)

trait Store[F[_]] {
  def get: F[Int]
  def getSignup: F[Signup]
  def getSignups: F[List[Signup]]
}

class MyStore(x: Int)(implicit val ec: ExecutionContext) extends Store[Future] {
  override def get: Future[Int] = Future(1)

  override def getSignup: Future[Signup] = ???

  override def getSignups: Future[List[Signup]] = ???
}

object App extends Directives {

  def store(implicit ec: ExecutionContext) = new MyStore(1)

  def getInt(implicit ec: ExecutionContext): Directive1[Int] =
    get & withRequestTimeout(2.seconds) & onSuccess(store.get)

  def getSignup: Directive1[Signup] =
    get & path("/") & provide(Signup(email = "???", password = "???"))

  def getSignups: Directive1[List[Signup]] =
    get & path("s") & provide(List.empty[Signup])

}
