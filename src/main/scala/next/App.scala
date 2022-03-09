package next

import akka.actor.{ ActorSystem, Scheduler }
import akka.event.BusLogging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings.{ ParserSettings, RoutingSettings, ServerSettings }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.pattern.CircuitBreaker
import akka.util.Timeout
import io.circe.generic.JsonCodec
import io.circe.parser._
import io.circe.syntax._
import io.circe.{ Decoder, Encoder }

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }
import scala.util._
import scala.util.control.NoStackTrace

@JsonCodec
case class Signup(email: String, password: String)

object Signup {

  final def decoder[A: Decoder](contentType: ContentType): FromEntityUnmarshaller[A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(contentType)
      .flatMap { ctx => mat => json =>
        decode[A](json).fold(Future.failed, Future.successful)
      }

  final def encoder[A: Encoder](contentType: ContentType): ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(contentType) { a =>
      HttpEntity(App.`application/vnd.app.v1+json`, a.asJson.noSpaces)
    }

  implicit lazy val decoder: FromEntityUnmarshaller[Signup] =
    decoder(App.`application/vnd.app.v1+json`)

  implicit lazy val encoder: ToEntityMarshaller[Signup] =
    encoder(App.`application/vnd.app.v1+json`)

  implicit lazy val listDecoder: FromEntityUnmarshaller[List[Signup]] =
    decoder(App.`application/vnd.app.v1+json`)

  implicit lazy val listEncoder: ToEntityMarshaller[List[Signup]] =
    encoder(App.`application/vnd.app.v1+json`)

}

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

  val `application/vnd.app.v1+json`: MediaType.WithFixedCharset =
    MediaType.applicationWithFixedCharset("vnd.app.v1+json", HttpCharsets.`UTF-8`)

  def store(implicit ec: ExecutionContext) = new MyStore(1)

  def getInt(implicit ec: ExecutionContext): Directive1[Int] =
    get & withRequestTimeout(2.seconds) & onSuccess(store.get)

  def getOptionInt: Directive1[Option[Int]] =
    get & provide(Option.empty[Int])

  def newSignup: Directive1[Signup] = post & entity(as[Signup])

  val aSignup = Signup(email = "???", password = "???")

  def getSignup: Directive1[Signup] = get & provide(aSignup)

  def getMaybeSignup1: Directive1[Option[Signup]] =
    get & provide(Option(aSignup))

  def getMaybeSignup: Directive1[Option[Signup]] = get & provide(Option.empty[Signup])

  def getTrySignup: Directive1[Try[Signup]] = get & provide(Try(aSignup))

  def getListSignup: Directive1[List[Signup]] = get & provide(List.empty[Signup])

  def toUUID(uuidAsString: String): Directive1[UUID] =
    scala.util.Try(UUID.fromString(uuidAsString)).fold(_ => reject, provide)

  def getUUID: Directive[Tuple1[UUID]] = (get & path(Segment)) flatMap toUUID

  def getUUID1: Directive1[UUID] = get & path(JavaUUID)

  def getPrivateUUID: Directive1[UUID] = pathPrefix("private") & get & pathPrefix(JavaUUID)

  def completeOptional[A: ToEntityMarshaller](option: Option[A]): Route =
    option.fold(complete(HttpResponse(status = StatusCodes.NotFound)))(it => complete(it))

  def completeTry[A: ToEntityMarshaller](aTry: Try[A]): Route =
    aTry.fold(failWith, it => complete(it))

  def getParamP: Directive[(Int, Int)] =
    /*get &*/ parameter("p".as[Int].optional).flatMap {
      case Some(p) => tprovide((p, 1))
      case None    => reject(ValidationRejection("missing p"))
    }

  object MissingP extends Rejection
  def getParamP2: Directive[(Int, Int)] =
    /*get &*/ parameter("p".as[Int].optional).flatMap {
      case Some(p) => tprovide((p, 1))
      case None    => reject(MissingP)
    }

  val rawRoute: Route = { ctx =>
    ctx.request.discardEntityBytes(ctx.materializer)
    ctx.complete("ok")
  }

  val MissingPRejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case `MissingP` => complete(StatusCodes.BadRequest, "missing p")
    }
    .result()
    .withFallback(RejectionHandler.default)

  object MissingPException extends RuntimeException with NoStackTrace
  def AppExceptionHandler(implicit settings: RoutingSettings): ExceptionHandler =
    ExceptionHandler {
      case App.`MissingPException` =>
        ctx => {
          ctx.request.discardEntityBytes(ctx.materializer)
          ctx.complete(StatusCodes.Conflict, "this is not right")
        }
    }.withFallback(ExceptionHandler.default(settings))
  def failing: Directive0 = throw MissingPException
}

object Main extends scala.App {

  implicit val system: ActorSystem                = ActorSystem(name = "vacation-rentals-app")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val log: BusLogging                    = new BusLogging(system.eventStream, system.name, this.getClass)
  implicit val timeout: Timeout                   = Timeout(5 seconds)
  val serverSettings = ServerSettings(system)
    .withParserSettings(
      ParserSettings
        .forServer(system)
        .withCustomMediaTypes(App.`application/vnd.app.v1+json`)
    )

  Http()
    .newServerAt(interface = "0.0.0.0", port = 8080)
    .withSettings(serverSettings)
    .bindFlow(App.getMaybeSignup(App.completeOptional))
}

object AppWithCircuitBreaker extends Directives {

  val resetTimeout: FiniteDuration = 1.second
  def createCircuitBreaker(implicit scheduler: Scheduler, executor: ExecutionContext) =
    new CircuitBreaker(
      scheduler,
      maxFailures = 1,
      callTimeout = 5.seconds,
      resetTimeout = resetTimeout
    )

  def divide(a: Int, b: Int)(implicit executor: ExecutionContext): Future[Int] =
    Future {
      a / b
    }

  def route(implicit scheduler: Scheduler, executor: ExecutionContext): Route = {
    val breaker = createCircuitBreaker
    path("divide" / IntNumber / IntNumber) { (a, b) =>
      onCompleteWithBreaker(breaker)(divide(a, b)) {
        case Success(value) =>
          complete(s"The result was $value")
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
    }
  }

}
