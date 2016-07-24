package io.truthencode.toad.verticle

import com.typesafe.config._
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.DeploymentOptions
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DeploymentOptionMergerTest extends FunSpec with Matchers with LazyLogging {

  trait DeploymentConfiguration {
    val dOptions = new DeploymentOptions()
    val cfgNone: Option[Config] = None
    val cfgDefault = Option(io.truthencode.toad.config.cfg)
  }

  describe("mergeConfig") {
    it("should add a default configuration to deployOptions") {
      new DeploymentConfiguration {
        //  assume(Option(dOptions.getConfig).isDefined)
        //   assume(cfgDefault.isDefined)
        val actual = dOptions.mergeConfig(cfgDefault)
        actual.getConfig should not be empty
      }
    }

    it("should add a default configuration to deployOptions if none supplied") {
      new DeploymentConfiguration {
        assume(Option(dOptions.getConfig).isEmpty)
        val actual = dOptions.mergeConfig(cfgNone)
        actual.getConfig should not be empty
      }
    }
  }
}
