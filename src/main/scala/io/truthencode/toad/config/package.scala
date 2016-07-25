package io.truthencode.toad

import com.typesafe.config._
import com.typesafe.scalalogging.slf4j.LazyLogging
import configs.Result.{Failure, Success}
import configs.syntax.ConfigOps
import io.truthencode.toad.actor.Actors
import io.truthencode.toad.verticle.VertxService
import io.vertx.core.json.JsonObject
import org.fusesource.scalate.TemplateEngine

import scala.language.implicitConversions

/**
  * Stores and / or retrieves general configuration information used by the sub-systems.
  * Also used as a base reference for Implicit objects.
  */
package object config extends LazyLogging {

  /**
    * Implicit services such as the template engine, Default Akka Actor system, Vertx main instance etc.
    */
  object Implicits {
    implicit lazy val engine = new TemplateEngine
    implicit lazy val system = Actors.actorSystem
    // Clustering orchestration for Vertx / Camel
    // implicit val mgr = new HazelcastClusterManager
    implicit lazy val vertx = VertxService.startVertx()

  }

  /**
    * Common convenience utilities for java Null / JSon Stringify etc
    */
  object CommonImplicits {
    implicit def jsonToString(j: JsonObject): String = j.encodePrettily()

    implicit def configToJsonObject(cfg: ConfigValue): JsonObject =
      new JsonObject(cfg.render(ConfigRenderOptions.concise()))

    implicit def toOpt[T](x: T): Option[T] = Option[T](x)

    implicit class ConfigValeUtil(c: ConfigValue) {
      def asJson: JsonObject = configToJsonObject(c)
    }

    implicit class ConfigObjectUtil(c: ConfigObject) {
      def asJson: JsonObject = new JsonObject(c.render(ConfigRenderOptions.concise()))

      def asJson(k: String): JsonObject = c.toConfig.getValue(k)
    }

  }

  val cfg = ConfigFactory.load("defaults")
  val (serverIp, serverPort, hostName) =
    cfg.get[ServerInfo]("server-info") match {
      case Success(x) => (x.ip, x.port, s"${x.hostName}:${x.port}")
      case Failure(x) =>
        logger.error("Error reading Server configuration", x.configException)
        ("127.0.0.1", "8080", "localhost:8080")
    }
}