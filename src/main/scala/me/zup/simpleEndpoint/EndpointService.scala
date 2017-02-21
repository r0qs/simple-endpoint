package me.zup.simpleEndpoint

import java.io.File
import org.parboiled.common.FileUtils
import scala.xml._
import akka.actor.Actor
import akka.event.Logging._
import akka.event.slf4j.SLF4JLogging
import spray.http._
import MediaTypes._
import spray.routing._
import spray.routing.directives.LogEntry
import spray.json._
import scala.util.matching.Regex

import me.zup.simpleEndpoint.AppConfig._

class PayloadGeneratorActor extends Actor with PayloadGenerator {
  def actorRefFactory = context
  def receive = runRoute(
    logRequestResponse(showRequestResponses _)(routes)
  )

  def showRequestResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(status, _, _, _) => Some(LogEntry(s"${request.method} ${request.uri} ($status)", InfoLevel))
    case response => Some(LogEntry(s"${request.method} ${request.uri} $response", WarningLevel))
  }
}

trait PayloadGenerator extends HttpService with SLF4JLogging {
  import me.zup.simpleEndpoint.DotJsonProtocol._
  import me.zup.simpleEndpoint.PayloadJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  //TODO: Rejects

  val jpattern = """\{"payload":\{"message":"(.*)"\}\}""".r;

  def toXML(pattern: Regex, json: String): scala.xml.Elem = json match {
    case jpattern(dots) => <payload><message>{ dots }</message></payload>
    case _ =>  <payload></payload>
  }

  def dataXML: scala.xml.Elem = {
    <data>
      <title>Test</title> 
      <userList>
        <user>
          <documentNumber>1111111</documentNumber>
          <name>LAVINIA SOUZA SANTOS</name>
          <account>739065</account>
          <branch>0001</branch>
        </user>
        <user>
          <documentNumber>2222222</documentNumber>
          <name>JOAO MARCOS DE SOUZA</name>
          <account>345545</account>
          <branch>0002</branch>
        </user>
        <user>
          <documentNumber>3333333</documentNumber>
          <name>JOSE MARIA DE JESUS</name>
          <account>233233</account>
          <branch>0003</branch>
        </user>
      </userList>
    </data>
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
  lazy val xmlData = dataXML

  val routes = {
    pathPrefix("json") {
      detach() {
        respondWithMediaType(`application/json`) {
          path("p") {
            complete(jp) 
          } ~
          path("m") {
            complete(jm) 
          } ~
          path("g") {
            complete(jg) 
          } ~
          path("gg") {
            complete(jgg) 
          }
        }
      }
    } ~
    pathPrefix("xml") {
      detach() {
        respondWithMediaType(`application/xml`) {
          path("users") {
            complete(xmlData)
          } ~
          path("p") {
            complete(xp)
          } ~
          path("m") {
            complete(xm)
          } ~
          path("g") {
            complete(xg)
          } ~
          path("gg") {
            complete(xgg)
          } 
        }
      }
    }
  }
}
