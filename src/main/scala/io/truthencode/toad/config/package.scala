package io.truthencode.toad

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import configs.Result.{Failure, Success}
import configs.syntax.ConfigOps
import io.truthencode.toad.actor.Actors
import io.truthencode.toad.verticle.VertxService
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.fusesource.scalate.TemplateEngine

package object config extends LazyLogging {

  object Implicits {
    implicit val engine = new TemplateEngine
    implicit val system = Actors.actorSystem
    // Clustering orchestration for Vertx / Camel
   // implicit val mgr = new HazelcastClusterManager
    implicit val vertx = VertxService.startVertx()

  }

  val config = ConfigFactory.load("defaults")
  val (serverIp, serverPort, hostName) =
    config.get[ServerInfo]("server-info") match {
      case Success(x) => (x.ip, x.port, s"${x.hostName}:${x.port}")
      case Failure(x) =>
        logger.error("Error reading Server configuration", x.configException)
        ("127.0.0.1", "8080", "localhost:8080")
    }
}