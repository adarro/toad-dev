package io.truthencode.toad.verticle;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a verticle. A verticle is a _Vert.x component_. This verticle is
 * implemented in Java, but you can implement them in JavaScript, Groovy or even
 * Ruby.
 */
public class MyFirstVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(MyFirstVerticle.class);
  private static final String COLLECTION = "whiskies";
  private MongoClient mongo;

  /**
   * This method is called when the verticle is deployed. It creates a HTTP
   * server and registers a simple request handler.
   * <p>
   * Notice the `listen` method. It passes a lambda checking the port binding
   * result. When the HTTP server has been bound on the port, it call the
   * `complete` method to inform that the starting has completed. Else it
   * reports the error.
   *
   * @param fut the future
   */
  @Override
  public void start(Future<Void> fut) {
//		try {
//			MongoService.Start();
//		} catch (IOException e) {
//			log.error("Failed to start Mongo",e);
//		}
    // Create a Mongo client
    mongo = MongoClient.createShared(vertx, config());
    if (config().size() > 0) {
      log.info("Mongo config");
      config().forEach(a -> {
        String key = a.getKey();
        Object value = a.getValue();
        log.info(String.format("k:%s\tv:%s", key, value));

      });
    } else {
      log.info("Using default Mongo config");
    }

    createSomeData(
      (nothing) -> startWebApp((http) -> completeStartup(http, fut)),
      fut);
  }

  /**
   * Method startWebApp ...
   *
   * @param next Handler<AsyncResult<HttpServer>> used to chain next deployment or event
   */
  private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
    // Create a router object.
    Router router = Router.router(vertx);

    // Bind "/" to our hello message.
    router.route("/")
      .handler(
        routingContext -> {
          HttpServerResponse response = routingContext
            .response();
          response.putHeader("content-type", "text/html")
            .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });

    router.route("/assets/*").handler(StaticHandler.create("assets"));

    router.get("/api/whiskies").handler(this::getAll);
    router.route("/api/whiskies*").handler(BodyHandler.create());
    router.post("/api/whiskies").handler(this::addOne);
    router.get("/api/whiskies/:id").handler(this::getOne);
    router.put("/api/whiskies/:id").handler(this::updateOne);
    router.delete("/api/whiskies/:id").handler(this::deleteOne);
    Config conf = ConfigFactory.load("defaults");
    int port = conf.getInt("server-info.port");
    //  Integer port = config().getInteger("http.port", 8080);
    log.info(String.format("Creating http server on port (%d)", port));
    // Create the HTTP server and pass the "accept" method to the request
    // handler.
    vertx.createHttpServer().requestHandler(router::accept).listen(
      // Retrieve the port from the configuration,
      // default to 8080.
      port, next);
  }

  /**
   * Listener used to handle completion events.
   *
   * Applies result to future.
   * @param http current status
   * @param fut result on completion of http
   */
  private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
    if (http.succeeded()) {
      fut.complete();
    } else {
      fut.fail(http.cause());
    }
  }

  /**
   * Cleans up resources upon stop request, specifically the mongo instance.
   * @throws Exception when processes fail to dispose.
   */
  @Override
  public void stop() throws Exception {
    mongo.close();
    //	MongoService.Stop();
  }

  private void addOne(RoutingContext routingContext) {
    final Whisky whisky = Json.decodeValue(
      routingContext.getBodyAsString(), Whisky.class);

    mongo.insert(
      COLLECTION,
      whisky.toJson(),
      r -> routingContext
        .response()
        .setStatusCode(201)
        .putHeader("content-type",
          "application/json; charset=utf-8")
        .end(Json.encodePrettily(whisky.setId(r.result()))));
  }

  private void getOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      mongo.findOne(
        COLLECTION,
        new JsonObject().put("_id", id),
        null,
        ar -> {
          if (ar.succeeded()) {
            if (ar.result() == null) {
              routingContext.response().setStatusCode(404)
                .end();
              return;
            }
            Whisky whisky = new Whisky(ar.result());
            routingContext
              .response()
              .setStatusCode(200)
              .putHeader("content-type",
                "application/json; charset=utf-8")
              .end(Json.encodePrettily(whisky));
          } else {
            routingContext.response().setStatusCode(404).end();
          }
        });
    }
  }

  private void updateOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    JsonObject json = routingContext.getBodyAsJson();
    if (id == null || json == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      mongo.updateCollection(
        COLLECTION,
        new JsonObject().put("_id", id), // Select a unique document
        // The update syntax: {$set, the json object containing the
        // fields to update}
        new JsonObject().put("$set", json),
        v -> {
          if (v.failed()) {
            routingContext.response().setStatusCode(404).end();
          } else {
            routingContext
              .response()
              .putHeader("content-type",
                "application/json; charset=utf-8")
              .end(Json.encodePrettily(new Whisky(id,
                json.getString("name"), json
                .getString("origin"))));
          }
        });
    }
  }

  private void deleteOne(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      mongo.removeDocuments(COLLECTION, new JsonObject().put("_id", id),
        ar -> routingContext.response().setStatusCode(204).end());
    }
  }

  private void getAll(RoutingContext routingContext) {
    mongo.find(
      COLLECTION,
      new JsonObject(),
      results -> {
        List<JsonObject> objects = results.result();
        List<Whisky> whiskies = objects.stream().map(Whisky::new)
          .collect(Collectors.toList());
        routingContext
          .response()
          .putHeader("content-type",
            "application/json; charset=utf-8")
          .end(Json.encodePrettily(whiskies));
      });
  }

  private void createSomeData(Handler<AsyncResult<Void>> next,
                              Future<Void> fut) {
    Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig",
      "Scotland, Islay");
    Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
    log.info(bowmore.toJson());

    // Do we have data in the collection ?
    mongo.count(COLLECTION, new JsonObject(), count -> {
      if (count.succeeded()) {
        if (count.result() == 0) {
          // no whiskies, insert data
          mongo.insert(COLLECTION, bowmore.toJson(), ar -> {
            if (ar.failed()) {
              fut.fail(ar.cause());
            } else {
              mongo.insert(COLLECTION, talisker.toJson(), ar2 -> {
                if (ar2.failed()) {
                  fut.failed();
                } else {
                  next.handle(Future.succeededFuture());
                }
              });
            }
          });
        } else {
          next.handle(Future.succeededFuture());
        }
      } else {
        // report the error
        fut.fail(count.cause());
      }
    });
  }
}
