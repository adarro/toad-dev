package io.truthencode.toad

import com.typesafe.scalalogging.LazyLogging
import io.vertx.codegen.annotations.Fluent
import io.vertx.core.{DeploymentOptions, Handler, Vertx}
import io.vertx.core.http.{HttpClientOptions, HttpClientResponse, HttpServer}
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MultiProtocolTest extends FunSpec with Matchers with LazyLogging with BeforeAndAfter {
  val vertx = Vertx.vertx()
  val PortSsl = 8443
  val PortNonSsl = 8888
  after {
    logger.info("closing vertx")
    vertx.close()
  }

  val webRoot: String = "werbroot"

  implicit class serverOps(source:HttpServer) {
    @Fluent
    def attachRoutes(): HttpServer = {
      val router = Router.router(vertx)
      router.route().handler(StaticHandler.create(webRoot).setIndexPage("index.html"))
      source.requestHandler(router.accept _)

    }
  }
  describe("HttpServer") {
    it("Should support ssl and nonssl") {
      val noSsl = vertx.createHttpServer()
      noSsl.attachRoutes().listen()
      noSsl.actualPort() shouldBe 80
    
      import io.truthencode.toad.verticle.DeploymentOptionMerger
      val deployOpt = new DeploymentOptions().mergeConfig()
      vertx.deployVerticle("io.truthencode.toad.verticle.WebSSLCapableServerVerticle",deployOpt)
      val client =vertx.createHttpClient(new HttpClientOptions().setSsl(true).setTrustAll(true))
      client.getNow(PortSsl,"localhost",  "/index.html", new Handler[HttpClientResponse]() {
        override def handle(event: HttpClientResponse): Unit = {
          event.statusCode() shouldBe 200
        }
      })
      
    
    }
  }
}
