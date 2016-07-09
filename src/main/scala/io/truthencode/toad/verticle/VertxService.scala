package io.truthencode.toad.verticle

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.{AsyncResult, Vertx, VertxOptions}

import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by adarr on 7/7/2016.
  */
object VertxService extends LazyLogging {

  import scala.concurrent.duration._

  val defaultDuration = 180 seconds

  def startVertx(timeout: Duration = defaultDuration) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val v = startVertXAsync()
    v.onComplete {
      case Success(x: Vertx) => x
      case Failure(ex) => throw ex
    }
    Await.result(v, timeout)
  }

  def startVertXAsync(): Future[Vertx] = {
    import Event2HandlerImplicits._
    val vLauncher = Promise[Vertx]
    val options = new VertxOptions() //.setClusterManager(cluster)

    Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
      if (evt.succeeded()) {
        logger.info("Vertx cluster successfully started")
        val v = evt.result()
        vLauncher.success(v)
        logger.warn("Using embedded MONGO. Change this before deploying to production!!!")
        v.deployVerticle("service:io.vertx.vertx-mongo-embedded-db", (ar3: AsyncResult[String]) => {
          if (ar3.succeeded())
            logger.info(s"Deployed embedded mongo: ${ar3.result()}")
          else
            logger.error("failed to deploy embedded mongo verticle", ar3.cause())
        })
        //        v.deployVerticle(new SimpleScalaVerticle, (ar2: AsyncResult[String]) => {
        //          if (ar2.succeeded())
        //            logger.info("We have Verticle liftoff :)")
        //          else {
        //            logger.error("Verticle-ly challenged!", ar2.cause())
        //          }
        //        })
      } else {
        logger.info("Failed to initialize Vertx cluster")
        vLauncher.failure(evt.cause())
      }
    })
    vLauncher.future
  }
}
