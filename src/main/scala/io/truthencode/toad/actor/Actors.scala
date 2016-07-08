package io.truthencode.toad.actor

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Actors {
  val config = ConfigFactory.load()
  val actorSystem = ActorSystem("openshift", config.getConfig("staticFiles").withFallback(config))

 // val handlerConfig = MyStaticHandlerConfig(actorSystem)

//  val staticContentHandlerRouter = actorSystem.actorOf(Props(new StaticContentHandler(handlerConfig))
//    .withRouter(FromConfig()).withDispatcher("my-pinned-dispatcher"), "static-file-router")
//  object MyStaticHandlerConfig extends ExtensionId[StaticContentHandlerConfig] with ExtensionIdProvider {
//    override def lookup = MyStaticHandlerConfig
//    override def createExtension(system: ExtendedActorSystem) =
//      new StaticContentHandlerConfig(system.settings.config, "my-static-content-handler")
//  }
}