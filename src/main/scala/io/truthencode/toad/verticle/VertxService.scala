package io.truthencode.toad.verticle

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.vertx.core.{AsyncResult, Vertx, VertxOptions}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.concurrent._
import scala.util.{Failure, Success}
import scala.language.postfixOps

/**
  * Created by adarr on 7/7/2016.
  */
object VertxService extends LazyLogging {
 // import io.truthencode.toad.config.Implicits.mgr
  def startVertx(timeout : Duration = 180 seconds) = {
    import ExecutionContext.Implicits.global
    val v = startVertXAsync()
    v.onComplete {
      case Success(x :Vertx) => x
      case Failure(ex) => throw ex
    }
    Await.result(v,timeout)
  }

  def startVertXAsync(): Future[Vertx] = {
    import Event2HandlerImplicits._
    val vLauncher = Promise[Vertx]
    val options = new VertxOptions()//.setClusterManager(cluster)

    Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
      if (evt.succeeded()) {
        logger.info("Vertx cluster successfully started")
        val v = evt.result()
        vLauncher.success(v)
        v.deployVerticle(new SimpleScalaVerticle, (ar2: AsyncResult[String]) => {
          if (ar2.succeeded())
            logger.info("We have Verticle liftoff :)")
          else {
            logger.error("Verticle-ly challenged!", ar2.cause())
          }
        })
      } else {
        logger.info("Failed to initialize Vertx cluster")
        vLauncher.failure(evt.cause())
      }
    })
    vLauncher.future
  }
}
