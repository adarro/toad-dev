package io.truthencode.toad

import com.netaporter.uri.dsl.{stringToUriDsl, uriToString, uriToUriOps}
import com.typesafe.scalalogging.LazyLogging
import io.truthencode.toad.config.{serverIp, serverPort}
import io.truthencode.toad.config.Implicits._
import io.truthencode.toad.verticle.{DeploymentOptionMerger, WebSSLCapableServerVerticle}
import io.vertx.core.{DeploymentOptions, Handler}
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse}
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

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

    lazy val api: String = subPath("/api")
    lazy val other: String = subPath("/web")
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

      val f = fixture
      val sslVerticle = classOf[WebSSLCapableServerVerticle].getName
      logger.info("Launching SSL Verticle")
      vertx.deployVerticle(sslVerticle, new DeploymentOptions().mergeConfig())
      logger.info("Launched vert")
      val cOpts = new HttpClientOptions()
        .setDefaultPort(8080)
        .setTrustAll(true)
        .setSsl(true)
      val client = vertx.createHttpClient(cOpts)
      val uriString = f.subPath("/")
      logger.debug(s"Attempting SSL call to $uriString")
      client.getNow(uriString, new Handler[HttpClientResponse] {
        override def handle(event: HttpClientResponse): Unit = {
          event.bodyHandler((buf: Buffer) => {
            val data = buf.getBytes.map(_.toChar).mkString
            logger.debug(s"Received Data $data")
          }
          )
        }

        //      client.getNow(uriString, (hcr: HttpClientResponse) => {
        //        hcr.bodyHandler((buf: Buffer) => {
        //          val rslt = buf.getBytes
        //
        //          logger.info(s"buff => $rslt")
        //        })
        //      })

      })
    }
  }

  describe("Non-existent resources") {
    it("Should return a NOT_IMPLEMENTED error") {
      // FIXME we routes should return 404 for not found and Not implemented for unsupported
      val f = fixture
      import f._
      val uri = other / "weapons" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)
      val cOpts = new HttpClientOptions()
        .setDefaultPort(8080)
        .setTrustAll(true)
        .setSsl(true)
      //   val c = HttpClients.createDefault()
      val client = vertx.createHttpClient(cOpts)
      client.getNow(uri, new Handler[HttpClientResponse] {
        override def handle(event: HttpClientResponse): Unit = {
          event.statusCode() should equal(404)
          event.bodyHandler((buf: Buffer) => {
            val data = buf.getBytes.map(_.toChar).mkString
            logger.debug(s"Received Data $data")
          })
        }
      })
    }
  }
}
