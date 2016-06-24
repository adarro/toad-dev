package io.truthencode.toad

import org.mashupbots.socko.webserver.{ WebServer, WebServerConfig }
import akka.actor.ActorSystem
import io.truthencode.toad.web.Routing
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.util.Properties
import scala.io.Source
import org.fusesource.scalate.TemplateEngine
import scala.collection.mutable.StringBuilder

object Bootstrap extends LazyLogging {
  implicit val system: ActorSystem = ActorSystem("OpenShift")
  implicit val engine = new TemplateEngine

  logger.info("configuring port and ip")
  // val src = Source.fromInputStream(getClass.getResourceAsStream("/app.properties")).bufferedReader()
  // Openshift variables are defined on the production server by openshift, we configure local values to run development
  val prop = new Properties()
  prop.load(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("app.properties")).bufferedReader())

  val (serverIp, serverPort, hostName) =
    (prop.getProperty("OPENSHIFT_DIY_IP"),
      prop.getProperty("OPENSHIFT_DIY_PORT"), prop.getProperty("HostName"))

  logger.info(s"ip: ${serverIp} port: ${serverPort}")

  val routes = Routing.Routes
  val webServer = new WebServer(WebServerConfig("OpenShift", serverIp, serverPort.toInt), routes, system)
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })
  webServer.start()
}