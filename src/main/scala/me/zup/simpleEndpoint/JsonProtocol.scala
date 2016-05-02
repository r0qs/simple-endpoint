package me.zup.simpleEndpoint

import spray.json._
import DefaultJsonProtocol._ 

/*
 * Define a custom json protocol for Dots and Payload.
 */

case class Dots(message: String)
case class Payload(payload: Dots)

object DotJsonProtocol extends DefaultJsonProtocol {
  implicit val dotFormat = jsonFormat1(Dots.apply)
}

object PayloadJsonProtocol extends DefaultJsonProtocol {
  implicit object PayloadJsonFormat extends RootJsonFormat[Payload] {
    import me.zup.simpleEndpoint.DotJsonProtocol._
    
    def write(p: Payload): JsValue = JsObject("payload" -> p.payload.toJson)
    
    def read(value: JsValue) = value match {
      case JsObject(m) => new Payload(Dots(m("payload").toString))
      case _ => deserializationError("Payload expected")
    }
  }
}

