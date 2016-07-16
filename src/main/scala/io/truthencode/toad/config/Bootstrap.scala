package io.truthencode.toad.config

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.JavaConverters._

/**
  * Configures and initializes template / routing etc engines.
  * Please see i.t.t.config Package object for important implicits in scope including the following:
  * Template engines (Jade / mustache etc) via i.t.t.a.Actors
  * Akka actor system
  * Vertx engine
  * Camel Routing
  * Vertx scala integration isn't quite there for 3.x so we'll do that portion in Java until then.
  */
object Bootstrap extends LazyLogging {

  logger.info(s"ip: $serverIp port: $serverPort @ $hostName")
  val serverInfo = ServerInfo(serverIp, serverPort, hostName)

  def init() = {
    import Implicits._
    logger.info("Deployed verticle ID's")
    for (id <- vertx.deploymentIDs().asScala) {
      logger.info(id)
    }
  }

  //Spin up Vertx
  // Let's create a simple vertx route
  //  val router = Router.router(vertx)
  //
  //  // Bind "/" to our hello message.
  //  router.route("/")
  //    .handler(
  //      new Handler[RoutingContext] {
  //        override def handle(ctx: RoutingContext) {
  //          ctx.response().putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>")
  //        }
  //      })
  // vertx.deployVerticle("io.truthencode.toad.verticle.MyFirstVerticle")

  //   val rh = requestHandler {
  //    new Handler[HttpServerRequest] {
  //      override def handle(httpServerRequest: HttpServerRequest): Unit = {
  //        router.accept(httpServerRequest)
  //      }
  //    }
  //  }

  //  val vertx = {
  //    val options = new VertxOptions().setClusterManager(mgr)
  //    Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
  //      if (evt.succeeded()) {
  //        logger.info("Vertx cluster successfully started")
  //        val v = evt.result()
  //        v.deployVerticle(new SimpleScalaVerticle, (ar2: AsyncResult[String]) => {
  //          if (ar2.succeeded())
  //            logger.info("We have Verticle liftoff :)")
  //          else {
  //            logger.error("Verticle-ly challenged!", ar2.cause())
  //          }
  //        })
  //      } else {
  //        logger.info("Failed to initialize Vertx cluster")
  //      }
  //    })
  //    Vertx.vertx()
  //  }

  //  vertx.createHttpServer().requestHandler {
  //    new Handler[HttpServerRequest] {
  //      override def handle(httpServerRequest: HttpServerRequest): Unit = {
  //        router.accept(httpServerRequest)
  //      }
  //    }
  //  }.listen(serverPort.toInt, serverIp, (ar: AsyncResult[HttpServer]) => {
  //    if (ar.succeeded()) {
  //      logger.info("Actually listening now")
  //
  //    }
  //    else {
  //      logger.error("Failed to attach Vert.X", ar.cause())
  //    }
  //
  //  })

  //Socko Webserver routing (should migrate to camel routing to vertx)

  /** val routes = Routing.Routes
    * val webServer = new WebServer(WebServerConfig("OpenShift", serverIp, serverPort.toInt), routes, system)
    * Runtime.getRuntime.addShutdownHook(new Thread {
    * override def run {
    * vertx.close()
    * webServer.stop()
    * }
    * })
    * webServer.start()
    * */

  /**
    * Displays configured ip, port and hostname to info logger.
    */
  def status() = {
    logger.info(s"server configured for @ $serverIp on port $serverPort, and should be available on $hostName")

  }

}