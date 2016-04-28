package br.com.zup.simpleEndpoint

import org.scalatest.FunSpec
import org.scalatest.Matchers
import spray.http._
import spray.json._
import StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService

import br.com.zup.simpleEndpoint.AppConfig._

class EndpointServiceSpec extends FunSpec
  with Matchers
  with ScalatestRouteTest
  with PayloadGenerator {

  def actorRefFactory = system

  describe("Endpoint service operations:") {
    import br.com.zup.simpleEndpoint.DotJsonProtocol._
    import br.com.zup.simpleEndpoint.PayloadJsonProtocol._
    import SprayJsonSupport._

    val jsonContent = jp.toJson.prettyPrint
    val xmlContent = xp.toString

    describe("GET") {
      it("should return a JSON with specific payload size") {
        Get("/json/p") ~> routes ~> check {
          val r = responseAs[String]
          status should be(OK)
          assert(r === jsonContent)
        }
      }
      it("should return a XML with specific payload size") {
        Get("/xml/p") ~> routes ~> check {
          val r = responseAs[String]
          status should be(OK)
          assert(r === xmlContent)
        }
      }
    }
  }
}
