package br.com.zup.simpleEndpoint

import org.scalatest.FunSpec
import org.scalatest.Matchers
import spray.http._
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
    val jsonContent = scala.io.Source.fromFile(jp).getLines.mkString
    val xmlContent = scala.io.Source.fromFile(xp).getLines.mkString

    describe("GET") {
      it("should return a JSON with specific payload size") {
        Get("/p/json") ~> routes ~> check {
          val r = responseAs[String]
          status should be(OK)
          assert(r === jsonContent)
        }
      }
      it("should return a XML with specific payload size") {
        Get("/p/xml") ~> routes ~> check {
          val r = responseAs[String]
          status should be(OK)
          assert(r === xmlContent)
        }
      }
    }
  }
}
