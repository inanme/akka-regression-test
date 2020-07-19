package com.example

package command {
  sealed trait Command
  case class Add(count: Int)      extends Command
  case class Subtract(count: Int) extends Command
  case object Print               extends Command
}

package event {
  trait Event
}

import event._

class EventProtoBufSerializer extends akka.serialization.SerializerWithStringManifest {
  override def identifier: Int = 9001

  // Event <- **Deserializer** <- Serialized(Event) <- Journal
  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case AddedManifest      => Added.parseFrom(bytes)
      case SubtractedManifest => Subtracted.parseFrom(bytes)
      case StateManifest      => State.parseFrom(bytes)
    }

  // We use the manifest to determine the event (it is called for us during serializing)
  // Akka will call manifest and attach it to the message in the event journal/snapshot database
  // when toBinary is being invoked
  override def manifest(o: AnyRef): String = o.getClass.getName

  final val AddedManifest      = classOf[Added].getName
  final val SubtractedManifest = classOf[Subtracted].getName
  final val StateManifest      = classOf[State].getName

  // Event -> **Serializer** -> Serialized(Event) -> Journal
  override def toBinary(o: AnyRef): Array[Byte] =
    o match {
      case a: Added      => a.toByteArray
      case s: Subtracted => s.toByteArray
      case s: State      => s.toByteArray
    }
}
