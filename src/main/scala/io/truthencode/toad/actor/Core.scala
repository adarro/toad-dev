package io.truthencode.toad.actor

import akka.actor.Actor
import java.util.Date
import org.mashupbots.socko.events.HttpRequestEvent
 
class Core extends Actor {
 
  def receive = {
 
    case event: HttpRequestEvent =>
      event.response.write("Hello from Socko (" + new Date().toString + ")")
      context.stop(self)
  }
 
}