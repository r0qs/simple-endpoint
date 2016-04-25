package br.com.zup.simpleEndpoint

import java.io.File
import org.parboiled.common.FileUtils
import scala.xml._
import akka.actor.Actor
import akka.event.Logging._
import akka.event.slf4j.SLF4JLogging
import spray.http.{HttpRequest, HttpResponse}
import spray.routing._
import spray.routing.directives.LogEntry
import spray.json._
import spray.http.HttpHeaders._
import spray.http.ContentTypes._
import scala.util.matching.Regex

import br.com.zup.simpleEndpoint.DotJsonProtocol._
import br.com.zup.simpleEndpoint.PayloadJsonProtocol._
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

  lazy val jp = Payload(Dots(readFile(p))).toJson.toString
  lazy val jm = Payload(Dots(readFile(m))).toJson.toString
  lazy val jg = Payload(Dots(readFile(g))).toJson.toString
  lazy val jgg = Payload(Dots(readFile(gg))).toJson.toString
  
  lazy val xp = toXML(jpattern, jp).toString
  lazy val xm = toXML(jpattern, jm).toString
  lazy val xg = toXML(jpattern, jg).toString
  lazy val xgg = toXML(jpattern, jgg).toString

  val routes = {
    pathPrefix("p") {
      detach() {
        path("json") {
          complete(jp)
        } ~
        path("xml") {
          complete(xp)
        }
      }
    } ~
  pathPrefix("m") {
    detach() {
      path("json") { 
        complete(jm)
      } ~
      path("xml") {
        complete(xm)
      }
    }
  } ~
  pathPrefix("g") {
    detach() {
      path("json") {
        complete(jg)
      } ~
      path("xml") {
        complete(xg)
      }
    }
  } ~
  pathPrefix("gg") {
    detach() {
      path("json") {
        complete(jgg)
      } ~
      path("xml") {
        complete(xgg)
      }
    }
  }
  }
}
