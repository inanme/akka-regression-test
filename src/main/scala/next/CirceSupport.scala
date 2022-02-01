package next

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypeRange, HttpEntity, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future

/**
  * To use circe for json marshalling and unmarshalling:
  */
object CirceSupport {
  private def jsonContentTypes: List[ContentTypeRange] =
    List(MediaTypes.`application/json`)

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap { ctx => mat => json =>
        decode[A](json).fold(Future.failed, Future.successful)
      }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(MediaTypes.`application/json`) { a =>
      HttpEntity(MediaTypes.`application/json`, a.asJson.noSpaces)
    }
}
