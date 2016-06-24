package io.truthencode.toad

import org.scalatest._
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.net.URI
import com.netaporter.uri.dsl._
import org.apache.http._

import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineRenderingTest extends FunSpec with Matchers with LazyLogging {
  lazy val fixture = new {
    import Bootstrap._
    val engine = Bootstrap.engine
    lazy val api = s"http://${serverIp}:${serverPort}/api"
    lazy val other = s"http://${serverIp}:${serverPort}/web"
    lazy val host = s"${serverIp}:${serverPort}"
  }

  describe("The template engine") {
    it("Should support and find jade") {
      val f = fixture
      import f._
      val data = engine.layout("/chat2.jade")
      logger.info(data)
    }
    it("Should support and find jade variables") {
      val f = fixture
      import f._
      val data = engine.layout("/chat2.jade",Map("host" -> f.host))
      logger.info(data)
      data shouldNot (contain("mysite"))
    }
  }
}