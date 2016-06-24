package io.truthencode.toad.web

import org.mashupbots.socko.events.HttpResponseStatus
import org.mashupbots.socko.routes.GET
import org.mashupbots.socko.routes.HttpRequest
import org.mashupbots.socko.routes.Path
import org.mashupbots.socko.routes.WebSocketHandshake

import com.typesafe.scalalogging.slf4j.LazyLogging

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.truthencode.toad.actor.Core
import io.truthencode.toad.actor.WebSocketHandler

object Routing extends LazyLogging {
  def Routes(implicit system: ActorSystem) = {
    org.mashupbots.socko.routes.Routes({
      case HttpRequest(httpRequest) => httpRequest match {
        case GET(Path("/api")) => {
          logger.info("Routing httpreq for /api to wsHandler")
          // Return HTML page to establish web socket
          system.actorOf(Props[WebSocketHandler]) ! httpRequest
        }
        
        case Path("/favicon.ico") => {
          logger.info("no favicon available")
          // If favicon.ico, just return a 404 because we don't have that file
          httpRequest.response.write(HttpResponseStatus.NOT_FOUND)
          //  org.mashupbots.socko.routes.
        }
      }
      case WebSocketHandshake(wsHandshake) => wsHandshake match {
        case Path("/api/") => {
          logger.info("authorizing wsHandshake for path api")
          // To start Web Socket processing, we first have to authorize the handshake.
          // This is a security measure to make sure that web sockets can only be established at your specified end points.
          wsHandshake.authorize()
        }
      }
      case GET(request) => {
        logger.info("generic get recieved")
        system.actorOf(Props[Core]) ! request
      }
    })
  }
}