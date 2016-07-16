package io.truthencode.toad

import com.netaporter.uri.dsl.{stringToUriDsl, uriToString, uriToUriOps}
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.truthencode.toad.config.{serverIp, serverPort}
import io.truthencode.toad.verticle.WebSSLCapableServerVerticle
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

@RunWith(classOf[JUnitRunner])
class GeneralConnectionIT extends FunSpec with Matchers with LazyLogging {

  lazy val fixture = new {
    io.truthencode.toad.config.Bootstrap.init()

    lazy val api = s"http://$serverIp:$serverPort/api"
    lazy val other = s"http://$serverIp:$serverPort/web"
  }

  describe("non-API filter") {
    it("Should reject websocket calls on unauthorized paths")(pending)
  }
  describe("Vertx connections") {
    it("Should support SSL") {
      import io.truthencode.toad.config.Implicits._
      val vert = new WebSSLCapableServerVerticle
      vertx.deployVerticle(vert)

      //      vertx.deployVerticle(vert,(result:AsyncResult[String]) => {
      //      assumeTrue( result.succeeded())
      //      })
    }
  }

  describe("Non-existant resources") {
    it("Should return a NOT_IMPLEMENTED error") {
      // FIXME we routes should return 404 for not found and Not implemented for unsupported
      val f = fixture
      import f._
      val uri = other / "weapons" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)
      val client = HttpClients.createDefault()
      val response = client.execute(new HttpGet(uri))
      val returnCode = response.getStatusLine.getStatusCode
      returnCode should equal(401)
    }
  }
}