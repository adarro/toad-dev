package io.truthencode.toad

import com.netaporter.uri.dsl.{stringToUriDsl, uriToString, uriToUriOps}
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.truthencode.toad.config.{serverIp, serverPort}
import io.truthencode.toad.verticle.Event2HandlerImplicits._
import io.truthencode.toad.verticle.{DeploymentOptionMerger, WebSSLCapableServerVerticle}
import io.vertx.core.DeploymentOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse}
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
    lazy val root = s"http://$serverIp:$serverPort"

    def subPath(p: String, prependSlash: Boolean = false): String = {
      if (prependSlash)
        s"$root/p"
      else
        s"$root$p"
    }

    def subPath(p: String): String = subPath(p, prependSlash = false)

    lazy val api = subPath("/api")
    lazy val other = subPath("/web")
  }

  describe("non-API filter") {
    it("Should reject websocket calls on unauthorized paths")(pending)
  }

  describe("Vertx connections") {
    ignore("should populate a context") {
      val vert = io.truthencode.toad.config.Implicits.vertx
      val ctx = vert.getOrCreateContext()
      ctx.config() should not be empty
    }
    it("Should support SSL") {
      import io.truthencode.toad.config.Implicits._
      val f = fixture
      val sslVerticle = classOf[WebSSLCapableServerVerticle].getName
      logger.info("Launching SSL Verticle")
      vertx.deployVerticle(sslVerticle, new DeploymentOptions().mergeConfig())
      logger.info("Launched vert")
      val cOpts = new HttpClientOptions()
        .setDefaultPort(8080)
      val client = vertx.createHttpClient(cOpts)

      client.getNow(f.subPath("/"), (hcr: HttpClientResponse) => {
        hcr.bodyHandler((buf: Buffer) => {
          logger.info(s"buff => ${buf.length()}")
        })
      })

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
      returnCode should equal(404)
    }
  }
}