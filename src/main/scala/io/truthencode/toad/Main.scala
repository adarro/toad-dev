package io.truthencode.toad

import org.mashupbots.socko.routes.{ Routes, GET }
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{ WebServer, WebServerConfig }
import io.truthencode.toad.actor.Core
import _root_.akka.actor.{ ActorSystem, Props }
import org.mashupbots.socko.events.HttpResponseStatus
import io.truthencode.toad.actor.WebSocketHandler
import io.truthencode.toad.web.Routing
import com.typesafe.scalalogging.slf4j.LazyLogging

object Main extends LazyLogging {

  import Bootstrap._

  def main(args: Array[String]) {
    val webServer = new WebServer(WebServerConfig("OpenShift", serverIp, serverPort.toInt), routes, system)
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })
    webServer.start()
  }

}