package io.truthencode.toad.config

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.truthencode.toad.config.CommonImplicits._
import io.truthencode.toad.verticle.DeploymentOptionMerger
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

/**
  * Created by adarr on 7/19/2016.
  */
@RunWith(classOf[JUnitRunner])
class ConfigurationTest extends FunSpec with Matchers with LazyLogging {
  describe("Typesafe Configuration") {
    it("should interpolate with Vert.x Configuration (Json)") {
      val key = "testwebserver"
      val control = new JsonObject().put(key,
        new JsonObject()
          .put("bind-address", "127.0.0.1")
          .put("bind-port", 8080)
          .put("webroot", "webroot")
          .put("ssl", true)
      )
      val expected = control.getJsonObject(key)
      val actual: JsonObject = ConfigFactory.load("sample").getValue(key)
      actual shouldEqual expected
    }
    it("should add typesafeconfig to deploymentoptions") {
      val dOpts = new DeploymentOptions().mergeConfig()
      dOpts.getConfig should not be empty
    }

    it("should be filterable from java.util.Maps") {
      val key = "webserver"
      logger.info("config keys =>")
      //      cfg.entrySet().asScala foreach {
      //        f => logger.info(s"k:${f.getKey}")
      //      }
      val filterMap = cfg.entrySet().asScala filter { x => x.getKey == key } map {
        case entry => new JsonObject().put(entry.getKey, entry.getValue)
      }

      val forComprehension = for {entry <- cfg.entrySet().asScala if entry.getKey equals key} yield new JsonObject().put(entry.getKey, entry.getValue)
      filterMap shouldEqual forComprehension
    }

  }

}
