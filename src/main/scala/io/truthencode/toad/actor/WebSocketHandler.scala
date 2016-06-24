package io.truthencode.toad.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.events.WebSocketFrameEvent
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import io.truthencode.toad.Bootstrap

class WebSocketHandler extends Actor with ActorLogging {
  /**
   * Process incoming messages
   */
  def receive = {
    case event: HttpRequestEvent =>
      log.info("httpevent recieved")
      // Return the HTML page to setup web sockets in the browser
      writeHTML(event)
      context.stop(self)
    case event: WebSocketFrameEvent =>
      // Echo web socket text frames
      log.info("wsFrame event recieved")
      writeWebSocketResponse(event)
      context.stop(self)
    case _ => {
      log.warning("received unknown message of type: ")
      context.stop(self)
    }
  }
  /**
   * Write HTML page to setup a web socket on the browser
   */
  private def writeHTML(ctx: HttpRequestEvent) {
    // Send 100 continue if required
    if (ctx.request.is100ContinueExpected) {
      ctx.response.write100Continue()
    }
    // FIXME change this template to a named file instead of chat2 
    log.info(s"setting host to ${Bootstrap.hostName}") 
    val template = Bootstrap.engine.layout("/chat2.jade",Map("host" -> Bootstrap.hostName))
    ctx.response.write(template, "text/html; charset=UTF-8")
  }

  /**
   * Echo the details of the web socket frame that we just received; but in upper case.
   */
  private def writeWebSocketResponse(frame: WebSocketFrameEvent) {
    log.info("TextWebSocketFrame: " + frame.readText)

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val time = new GregorianCalendar()
    val ts = dateFormatter.format(time.getTime())

    frame.writeText(ts + " " + frame.readText.toUpperCase())
  }
}