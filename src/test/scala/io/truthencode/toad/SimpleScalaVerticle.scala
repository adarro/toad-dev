package io.truthencode.toad

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import io.truthencode.toad
import io.truthencode.toad.config.CommonImplicits._
import io.truthencode.toad.verticle.Whisky
import io.truthencode.toad.verticle.Event2HandlerImplicits._
import _root_.io.vertx.core.{AbstractVerticle, AsyncResult, Handler, _}
import _root_.io.vertx.core.http.HttpServer
import _root_.io.vertx.core.json.{Json, JsonObject}
import _root_.io.vertx.ext.mongo.{MongoClient, MongoClientDeleteResult, MongoClientUpdateResult}
import _root_.io.vertx.ext.web.{Router, RoutingContext}
import _root_.io.vertx.ext.web.handler.{BodyHandler, StaticHandler}
import scala.collection.JavaConverters._

object SimpleScalaVerticle extends LazyLogging {
  private val log = logger
  private val COLLECTION = "whiskies"
}

class SimpleScalaVerticle extends AbstractVerticle {
  private[this] lazy val mongo = MongoClient.createShared(vertx, config)

  override def start(fut: Future[Void]) {
    if (config.size > 0) {
      toad.SimpleScalaVerticle.log.info("Mongo config")
      config.getMap.forEach((k,v) => toad.SimpleScalaVerticle.log.info(s"k:$k\tv:$v"))      
    } else {
      toad.SimpleScalaVerticle.log.info("Using default Mongo config")
    }
    createSomeData((_: AsyncResult[Void]) => {
      startWebApp((http: AsyncResult[HttpServer]) => completeStartup(http, fut))
    }, fut)
  }

  private def startWebApp(next: Handler[AsyncResult[HttpServer]]) {
    SimpleScalaVerticle.log.info("Starting verticle asyncly")
    val router: Router = Router.router(vertx)
    router
      .route("/")
      .handler(new Handler[RoutingContext] {
        override def handle(ctx: RoutingContext) {
          ctx
            .response()
            .putHeader("content-type", "text/html")
            .end("<h1>Hello from my first scala verticle with Vert.x 3 application</h1>")
        }
      })

    router
      .route("/test/")
      .handler((x: RoutingContext) =>
        toad.SimpleScalaVerticle.log.info("we did something"))

    router
      .route("/")
      .handler((c: RoutingContext) => {
        c.response
          .putHeader("content-type", "text/html")
          .end("<h1>Hello from my first Vert.x 3 application</h1>")
      })
    router.route("/assets/*").handler(StaticHandler.create("assets"))
    router.get("/api/whiskies").handler(this.retrieveAll _)
    router
      .route("/api/whiskies*")
      .handler(new Handler[RoutingContext] {
        override def handle(ctx: RoutingContext) {
          BodyHandler.create()
        }
      })
    // router.route("/api/whiskies*").handler((c:RoutingContext) => { BodyHandler.create()} )
    router.post("/api/whiskies").handler(this.addOne _)
    router.get("/api/whiskies/:id").handler(this.retrieveOne _)
    router.put("/api/whiskies/:id").handler(this.updateOne _)
    router.delete("/api/whiskies/:id").handler(this.deleteOne _)
    val conf: Config = ConfigFactory.load("defaults")
    val port: Int = conf.getInt("server-info.port")
    toad.SimpleScalaVerticle.log.info(s"Creating http server on port $port")
    vertx.createHttpServer
      .requestHandler(router.accept _)
      .listen(port, next.handle _)
  }

  private def addOne(routingContext: RoutingContext) {
    val whisky: Whisky =
      Json.decodeValue(routingContext.getBodyAsString, classOf[Whisky])
    mongo.insert(
      toad.SimpleScalaVerticle.COLLECTION,
      whisky.toJson,
      new Handler[AsyncResult[String]] {
        override def handle(r: AsyncResult[String]): Unit = {
          routingContext
            .response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(whisky.setId(r.result())))
        }
      }
    )
  }

  private def retrieveOne(routingContext: RoutingContext) {
    Option(routingContext.request.getParam("id")) match {
      case Some(id) =>
        mongo.findOne(
          toad.SimpleScalaVerticle.COLLECTION,
          new JsonObject().put("_id", id),
          null,
          (ar: AsyncResult[JsonObject]) => {
            if (ar.succeeded()) {
              if (ar.result() == null) {
                routingContext
                  .response()
                  .setStatusCode(404)
                  .end()
                return
              }
              val whisky = new Whisky(ar.result())
              routingContext
                .response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky))
            } else {
              routingContext.response().setStatusCode(404).end()
            }
          }
        )
      case None => routingContext.response.setStatusCode(400).end()
    }
  }

  private def updateOne(routingContext: RoutingContext) {
    val id: String = routingContext.request.getParam("id")
    val json: JsonObject = routingContext.getBodyAsJson
    if (id == null || json == null) {
      routingContext.response.setStatusCode(400).end()
    } else {
      mongo.updateCollection(
        toad.SimpleScalaVerticle.COLLECTION,
        new JsonObject().put("_id", id),
        new JsonObject().put("$set", json),
        (v: AsyncResult[MongoClientUpdateResult]) => {
          if (v.failed()) {
            routingContext.response().setStatusCode(404).end()
          } else {
            routingContext
              .response()
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(
                Json.encodePrettily(
                  new Whisky(id,
                    json.getString("name"),
                    json
                      .getString("origin"))))
          }
        }
      )
    }
  }

  private def deleteOne(routingContext: RoutingContext) {
    val x: Option[String] = routingContext.request.getParam("id")
    x match {
      case Some(id) =>
        mongo.removeDocument(
          toad.SimpleScalaVerticle.COLLECTION,
          new JsonObject().put("_id", id),
          (ar: AsyncResult[MongoClientDeleteResult]) => {
            if (ar.succeeded())
              routingContext.response().setStatusCode(204).end()
            else {
              val t = ar.result()
              routingContext
                .response()
                .setStatusMessage(s"removed ${t.getRemovedCount} documents")
                .setStatusCode(400)
                .end()
            }
          }
        )
      case None => routingContext.response.setStatusCode(400).end()
    }
  }

  private def retrieveAll(routingContext: RoutingContext) {

    mongo.find(
      toad.SimpleScalaVerticle.COLLECTION,
      new JsonObject,
      (results: AsyncResult[java.util.List[JsonObject]]) => {
        val objects = results.result().asScala       
        val whiskies = objects.map((o: JsonObject) => {
          new Whisky(o)
        })
        routingContext
          .response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(whiskies))
      }
    )
  }

  private def completeStartup(http: AsyncResult[HttpServer],
                              fut: Future[Void]) {
    if (http.succeeded) {
      fut.complete()
    } else {
      fut.fail(http.cause)
    }
  }

  private def createSomeData(next: Handler[AsyncResult[Void]],
                             fut: Future[Void]) {
    val bowmore: Whisky =
      new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay")
    val talisker: Whisky = new Whisky("Talisker 57° North", "Scotland, Island")
    SimpleScalaVerticle.log.info(bowmore.toJson)
    val aResult2 = (ar2: AsyncResult[String]) => {
      if (ar2.failed()) {
        fut.fail(ar2.cause())
      } else {
        ok[Void]
      } //elif
    }
    val aResult = (ar: AsyncResult[String]) => {
      if (ar.failed()) {
        fut.fail(ar.cause())
      } else {
        mongo.insert(SimpleScalaVerticle.COLLECTION, talisker.toJson, aResult2)
      } //elif
    }

    val countResult = (count: AsyncResult[java.lang.Long]) => {
      if (count.succeeded())
        if (count.result() == 0) {
          // no whiskies, insert data
          mongo.insert(SimpleScalaVerticle.COLLECTION, bowmore.toJson, aResult)
        } else {
          //  next.handle(Future[Void].succeededFuture())
          next.handle(ok[Void])
        } else {
        // report the error
        fut.fail(count.cause())
      }
    }
    mongo.count(SimpleScalaVerticle.COLLECTION, new JsonObject, countResult)
  }

  private def ok[T] = {
    import Future.{succeededFuture => suss}
    suss[T]
  }

  @throws[Exception]
  override def stop() {
    mongo.close()
  }

}
