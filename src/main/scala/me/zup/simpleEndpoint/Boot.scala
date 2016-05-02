package me.zup.simpleEndpoint

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App {
  import me.zup.simpleEndpoint.AppConfig._

  implicit val system = ActorSystem("EndpointSystem")

  val service = system.actorOf(Props[PayloadGeneratorActor], "PayloadGenerator")

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = AppConfig.interface, port = AppConfig.port)
}
