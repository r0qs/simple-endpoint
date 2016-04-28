package br.com.zup.simpleEndpoint

import java.io.File
import org.parboiled.common.FileUtils
import scala.xml._
import akka.actor.Actor
import akka.event.Logging._
import akka.event.slf4j.SLF4JLogging
import spray.http._
import MediaTypes._
import spray.httpx.marshalling._
import spray.routing._
import spray.routing.directives.LogEntry
import spray.json._
import scala.util.matching.Regex

import br.com.zup.simpleEndpoint.AppConfig._

class PayloadGeneratorActor extends Actor with PayloadGenerator {
  def actorRefFactory = context
  def receive = runRoute(
    logRequestResponse(showRequestResponses _)(routes)
  )

  // Log each request and response.
  def showRequestResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(status, _, _, _) => Some(LogEntry(s"${request.method} ${request.uri} ($status)", InfoLevel))
    case response => Some(LogEntry(s"${request.method} ${request.uri} $response", WarningLevel))
  }
}

trait PayloadGenerator extends HttpService with SLF4JLogging {
  import br.com.zup.simpleEndpoint.DotJsonProtocol._
  import br.com.zup.simpleEndpoint.PayloadJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val jpattern = """\{"payload":\{"message":"(.*)"\}\}""".r;

  def toXML(pattern: Regex, json: String): scala.xml.Elem = json match {
    case jpattern(dots) => <payload><message>{ dots }</message></payload>
    case _ =>  <payload></payload>
  }

  def createFile(fileName: String, size: Int): File = {
    val file = new File(s"$rootDataDir/" + fileName)
    if (! file.exists) {
      log.info("Creating file: {} with size: {} bytes", fileName, size)
      FileUtils.writeAllText(List.fill(size)(".").par.mkString, file)
    }
    file
  }

  def readFile(file: File): String = scala.io.Source.fromFile(file).getLines.mkString

  val p  = createFile("p", small)
  val m  = createFile("m", medium)
  val g  = createFile("g", big)
  val gg = createFile("gg", large)

  lazy val jp = Payload(Dots(readFile(p)))
  lazy val jm = Payload(Dots(readFile(m)))
  lazy val jg = Payload(Dots(readFile(g)))
  lazy val jgg = Payload(Dots(readFile(gg)))
  
  lazy val xp = toXML(jpattern, jp.toJson.toString)
  lazy val xm = toXML(jpattern, jm.toJson.toString)
  lazy val xg = toXML(jpattern, jg.toJson.toString)
  lazy val xgg = toXML(jpattern, jgg.toJson.toString)

  val routes = {
    pathPrefix("p") {
      detach() {
        path("json") {
          respondWithMediaType(`application/json`) {
            complete(jp) 
          }
        } ~
        path("xml") {
          respondWithMediaType(`application/xml`) {
            complete(xp)
          }
        }
      }
    } ~
  pathPrefix("m") {
    detach() {
      path("json") {
        respondWithMediaType(`application/json`) {
          complete(jm)
        }
      } ~
      path("xml") {
        respondWithMediaType(`application/xml`) {
          complete(xm)
        }
      }
    }
  } ~
  pathPrefix("g") {
    detach() {
      path("json") {
        respondWithMediaType(`application/json`) {
          complete(jg)
        }
      } ~
      path("xml") {
        respondWithMediaType(`application/xml`) {
          complete(xg)
        }
      }
    }
  } ~
  pathPrefix("gg") {
    detach() {
      path("json") {
        respondWithMediaType(`application/json`) {
          complete(jgg)
        }
      } ~
      path("xml") {
        respondWithMediaType(`application/xml`) {
          complete(xgg)
        }
      }
    }
  }
  }
}
