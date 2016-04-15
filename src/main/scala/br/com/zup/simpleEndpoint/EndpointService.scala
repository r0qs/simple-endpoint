package br.com.zup.simpleEndpoint

import java.io.File
import org.parboiled.common.FileUtils
import scala.math._
import scala.xml._
import akka.actor._
import spray.routing._
import spray.http._
import spray.json._
import scala.util.matching.Regex

import br.com.zup.simpleEndpoint.DotJsonProtocol._
import br.com.zup.simpleEndpoint.PayloadJsonProtocol._
import br.com.zup.simpleEndpoint.AppConfig._

class PayloadGeneratorActor extends Actor with PayloadGenerator {
  def actorRefFactory = context
  def receive = runRoute(routes)
}

trait PayloadGenerator extends HttpService { 
  /* TODO: 
   * - create actors to create the files in parallel and selfdestroy after creation
   * - Use chunks (MessageChunk) to send large data (Akka streams)
   */

  val files: Map[String, String] = Map(
    "jp"  -> s"$rootDataDir/p.json",
    "jm"  -> s"$rootDataDir/m.json",
    "jg"  -> s"$rootDataDir/g.json",
    "jgg" -> s"$rootDataDir/gg.json",
    "xp"  -> s"$rootDataDir/p.xml",
    "xm"  -> s"$rootDataDir/m.xml",
    "xg"  -> s"$rootDataDir/g.xml",
    "xgg"  -> s"$rootDataDir/gg.xml"
  )

  lazy val jp  = createFile("jp", small, "json")
  lazy val jm  = createFile("jm", medium, "json")
  lazy val jg  = createFile("jg", big, "json")
  lazy val jgg = createFile("jgg", large, "json")

  lazy val xp  = createFile("xp", small, "xml")
  lazy val xm  = createFile("xm", medium, "xml")
  lazy val xg  = createFile("xg", big, "xml")
  lazy val xgg = createFile("xgg", large, "xml")

  val jpattern = """\{"payload":\{"message":"(.*)"\}\}""".r;

  def toXML(pattern: Regex, json: String): scala.xml.Elem = json match {
    case jpattern(dots) => <payload><message>{ dots }</message></payload>
    case _ =>  <payload></payload>
  }

  def createFile(fileName: String, size: Int, fileExtension: String): File = {
    val file = new File(files(fileName))
    if (! file.exists) {
      fileExtension match {
        case "json" => FileUtils.writeAllText(Payload(Dots(List.fill(size)(".").par.mkString)).toJson.toString, file)
        case "xml" =>
          val jsonFileName = "j" + fileName.splitAt(1)._2
          val content = scala.io.Source.fromFile(files(jsonFileName)).getLines.mkString
          FileUtils.writeAllText(toXML(jpattern, content).toString, file)
      }
    }
    file
  }

  val routes = {
    get {
      pathPrefix("p") {
        detach() {
          path("json") { 
            getFromFile(jp)
          } ~
          path("xml") {
            getFromFile(xp)
          }
        }
      } ~
      pathPrefix("m") {
        detach() {
          path("json") { 
            getFromFile(jm)
          } ~
          path("xml") {
            getFromFile(xm)
          }
        }
      } ~
      pathPrefix("g") {
        detach() {
          path("json") { 
            getFromFile(jg)
          } ~
          path("xml") {
            getFromFile(xg)
          }
        }
      } ~
      pathPrefix("gg") {
        detach() {
          path("json") { 
            getFromFile(jgg)
          } ~
          path("xml") {
            getFromFile(xgg)
          }
        }
      }
    }
  }
}
