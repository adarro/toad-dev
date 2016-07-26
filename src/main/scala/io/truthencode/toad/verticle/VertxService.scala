package io.truthencode.toad.verticle

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.truthencode.toad.config.CommonImplicits._
import io.truthencode.toad.config.cfg
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.{AsyncResult, DeploymentOptions, Vertx, VertxOptions}

import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by adarr on 7/7/2016.
  */
object VertxService extends LazyLogging {

  import scala.concurrent.duration._

  val defaultDuration = 180 seconds

  /**
    * Initializes the Vertx engine synchronously
    *
    * @note internally calls [[startVertXAsync()]] with a blocking call.
    * @param timeout maximum wait time to block before failing. (defaults to [[defaultDuration]]
    * @return A Vertx instance or execption in the case of failure.
    */
  def startVertx(timeout: Duration = defaultDuration) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val v = startVertXAsync()
    v.onComplete {
      case Success(x: Vertx) => x
      case Failure(ex) => logger.error("Failed to start Vertx", ex)
        throw ex
    }
    Await.result(v, timeout)
  }

  private def toBilliSeconds(l: Long) = {
    l * 1000 * 1000000
  }

  /**
    * Initializes a clustered vertx instance Asynchronously
    *
    * @return Initialized Future containing Vertx instance or exception
    */
  def startVertXAsync(): Future[Vertx] = {
    import Event2HandlerImplicits._
    val vLauncher = Promise[Vertx]

    val addOpts = new AddressResolverOptions()
      .setCacheMinTimeToLive(90) //90 second min cache ttl
      .setCacheNegativeTimeToLive(90)
    // FIXME: change MaxEventLoopExecutionTime back to a more reasonable number or fix blocking to accept the default
    val blockDelay: Long = toBilliSeconds(10)
    val options = new VertxOptions()
      .setMaxEventLoopExecuteTime(blockDelay)
      .setAddressResolverOptions(addOpts)
    Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
      if (evt.succeeded()) {
        logger.info("Vertx cluster successfully started")
        val v = evt.result()
        logger.info("Creating base context")
        // FIXME: currently merging from a typesafe config that may contain extra data (i.e. akka) should prune this closer to production

        v.getOrCreateContext().config().mergeIn(cfg.root)
        vLauncher.success(v)
        val sslVerticle = classOf[WebSSLCapableServerVerticle].getName
        logger.info("Launching SSL Verticle")
        v.deployVerticle(sslVerticle, new DeploymentOptions().mergeConfig())

      } else {
        logger.info("Failed to initialize Vertx cluster")
        vLauncher.failure(evt.cause())
      }
    })
    vLauncher.future
  }
}
