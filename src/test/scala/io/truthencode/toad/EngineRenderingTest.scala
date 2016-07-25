package io.truthencode.toad

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.truthencode.toad.config.Implicits.engine
import io.truthencode.toad.config.{serverIp, serverPort}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class EngineRenderingTest extends FunSpec with Matchers with LazyLogging {
  lazy val fixture = new {   

   // val engine = engine
    lazy val api = s"http://$serverIp:$serverPort/api"
    lazy val other = s"http://$serverIp:$serverPort/web"
    lazy val host = s"$serverIp:$serverPort"
  }

  describe("The template engine") {
    it("Should support and find jade") {
      val f = fixture

      val data = engine.layout("/chat2.jade")
      logger.info(data)
    }

    it("Should support and find jade variables") {
      import scala.language.reflectiveCalls
      val f = fixture

      val data = engine.layout("/chat2.jade",Map("host" -> f.host))
      logger.info(data)
      data shouldNot contain("mysite")
    }
  }
}