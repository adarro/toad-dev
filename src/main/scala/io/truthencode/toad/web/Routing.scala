package io.truthencode.toad.web

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
@deprecated(message = "No longer using SocketMonkey, routing is done by Vertx",since="0.0.1")
object Routing extends LazyLogging {
  def Routes(implicit system: ActorSystem) = {
//    org.mashupbots.socko.routes.Routes({
//      case HttpRequest(httpRequest) => httpRequest match {
//        case GET(Path("/api")) => {
//          logger.info("Routing httpreq for /api to wsHandler")
//          // Return HTML page to establish web socket
//          system.actorOf(Props[WebSocketHandler]) ! httpRequest
//        }
//
//        case Path("/favicon.ico") => {
//          logger.info("no favicon available")
//          // If favicon.ico, just return a 404 because we don't have that file
//          httpRequest.response.write(HttpResponseStatus.NOT_FOUND)
//          //  org.mashupbots.socko.routes.
//        }
//        case Path("/") => {
//          logger.info("default path requested...")
//          val file = new File("/", "index.html")
//          logger.info(s"file ${file.getCanonicalFile.toString()} exists: ${file.exists}")
//          staticContentHandlerRouter ! new StaticFileRequest(httpRequest, file)
//        }
//        case _ => {
//          logger.info("unsupported request recieved")
//          httpRequest.response.write(HttpResponseStatus.NOT_IMPLEMENTED)
//        }
//      }
//      case WebSocketHandshake(wsHandshake) => wsHandshake match {
//        case Path("/api/") => {
//          logger.info("authorizing wsHandshake for path api")
//          // To start Web Socket processing, we first have to authorize the handshake.
//          // This is a security measure to make sure that web sockets can only be established at your specified end points.
//          wsHandshake.authorize()
//        }
//      }
//      case GET(request) => {
//        logger.info("generic get recieved")
//        system.actorOf(Props[Core]) ! request
//      }
//      case _ => {
//        throw new NotImplementedError
//      }
//
//    })
  }
}