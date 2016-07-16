package io.truthencode.toad.config

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.slf4j.LazyLogging
import configs.Result.{Failure, Success}
import org.junit.Assume._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

import scala.language.postfixOps
import scala.util.Try

/**
  * Created by adarr on 7/15/2016.
  */
@RunWith(classOf[JUnitRunner])
class DbInfoTest extends FunSpec with Matchers with LazyLogging {
  val fixture = new {
    logger.info("Forcing Logger Init")
  }
  describe("Database Configuration") {
    it("Should be backed by a valid config file by default") {
      val check = ConfigFactory.parseString(
        """
     db-host {
          |host=mysever.com
          |  port=8889
          |  uuid=default
          |  userId=joe
          |  pwd=secret
          |  url=mongodb":"//myserver.com":"8989
          |}
        """.stripMargin)
      assumeTrue("safety check was not resolved", check.isResolved)
      assumeTrue("Config was not resolved", config.isResolved)
      noException should be thrownBy config.checkValid(check, "db-info")
    }

    it("should have sensible defaults") {
      import configs.syntax.ConfigOps
      //   val (host,port,uuid,userId,pwd,url) =

      val tCfg = Try(config.resolve) match {
        case x: Try[Config] => Some(x.get)
        case _ => None
      }
      tCfg should not be empty
      val cfg = tCfg.get
      val result = cfg.get[DbInfo]("db-info") match {
        case Success(x) => Some(DbInfo(x.host, x.port, x.uuid, x.userId, x.pwd, x.url))
        case Failure(x) =>
          val msg = "Error reading Server configuration"
          logger.error(msg, x.configException)
          None
      }
      result should not be empty
    }

    it("Should be read from configuration files ") {
      val dbConfig = Try(DbInfo.apply) match {
        case x: Try[DbInfo] => x.get
      }

      dbConfig.port should not equal 0
      dbConfig.host should not be null
      dbConfig.url should not be null
      dbConfig.userId should not be null
      dbConfig.pwd should not be null
      dbConfig.pwd.length should be > 0
    }
  }
}
