package io.truthencode.toad.verticle

import java.time
import java.time.Instant

import com.typesafe.scalalogging.LazyLogging
import configs.syntax._
import io.truthencode.toad.config
import io.truthencode.toad.config.cfg
import io.truthencode.toad.config.CommonImplicits._
import io.vertx.core._
import io.vertx.core.dns.AddressResolverOptions
import configs.Result.{Failure => CFGFailure, Success => CFGSuccess}
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

import scala.concurrent.{Await, Future, Promise, TimeoutException}
import scala.language.postfixOps
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * Created by adarr on 7/7/2016.
  */
object VertxService extends LazyLogging {

  import scala.concurrent.duration._

  implicit def asFiniteDuration(d: java.time.Duration) =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  val defaultDuration = 30 seconds

  /**
    * Initializes the Vertx engine synchronously
    *
    * @note internally calls [[startVertXAsync()]] with a blocking call.
    * @param timeout maximum wait time to block before failing. (defaults to [[defaultDuration]])
    * @return A Vertx instance or exception in the case of failure.
    */
  def startVertx(timeout: Duration = defaultDuration) = {
    logger.info(s"Starting Vertx async with Timeout $timeout")

    val ts = Instant.now()
    import scala.concurrent.ExecutionContext.Implicits.global
    val v = startVertXAsync()
    v.onComplete {
      case Success(x: Vertx) =>
        logger.debug("start vertx onComplete succeeded")
        /*val server = x.createHttpServer()
        logger.info(s"Start Completed in ${timeStat(ts)}")
        logger.info("adding routes")
        val config = x.getOrCreateContext().config().getJsonObject("webserver")

        val webRoot: String = config.getString("webroot", "webroot/")
        val router = Router.router(x)
        router.route().handler(StaticHandler.create(webRoot).setIndexPage("index.html"))
        server.requestHandler(router.accept _).listen(io.truthencode.toad.config.serverPort.toInt)
        x*/
      case Failure(ex: TimeoutException) =>
        logger.error(s"Failed to start Vertx in timely fashion ${timeStat(ts)}", ex)
        throw ex
      case Failure(ex) => logger.error(s"Failed to start Vertx", ex)
    }
    logger.info("awaiting start up")
    val r = Await.result(v, timeout)
    logger.info(s"Deployed verticles ${r.deploymentIDs()}")
    r
  }

  def timeStat(instant: Instant): time.Duration = {
    java.time.Duration.between(instant, Instant.now())
  }

  /**
    * Initializes a clustered vertx instance Asynchronously
    *
    * @return Initialized Future containing Vertx instance or exception
    */
  def startVertXAsync(): Future[Vertx] = {
    val ts = Instant.now()
    val clusterManager = config.ClusterManager
    val vLauncher = Promise[Vertx]
    val vOptions = new VertxOptions().setClusterManager(config.Implicits.cluster)
    val addOpts = new AddressResolverOptions()
      .setCacheMinTimeToLive(90) //90 second min cache ttl
      .setCacheNegativeTimeToLive(90)
    // FIXME: change MaxEventLoopExecutionTime back to a more reasonable number or fix blocking to accept the default
    val blockDelay: Long = toBilliSeconds(10)
    val options = vOptions
      .setMaxEventLoopExecuteTime(blockDelay)
      .setAddressResolverOptions(addOpts)
    Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
      if (evt.succeeded()) {
        logger.info(s"Vertx cluster successfully started in ${timeStat(ts)}")
        val v = evt.result()
        logger.info("Creating base context")
        // FIXME: currently merging from a typesafe config that may contain extra data (i.e. akka) should prune this closer to production
        val ctx = v.getOrCreateContext()
        logger.debug("Acquired vertx context, merging config")
        ctx.config().mergeIn(cfg.root)
        logger.debug("merging deployment options config")
        ctx.addCloseHook((completionHandler: Handler[AsyncResult[Void]]) => {

          v.deploymentIDs().forEach{vx =>
            v.undeploy(vx, (event: AsyncResult[Void]) => {
              logger.debug(s"Undeployed Verticle $vx")
            })
          }

        })
        val deployOpt = new DeploymentOptions().mergeConfig()
        logger.debug("Deploying verticles")
        cfg.get[List[String]]("verticles") match {
          case CFGSuccess(x) => x.foreach { t =>
            logger.info(s"launching $t")
            Try( v.deployVerticle(t, deployOpt)) match {
              case Failure(ex) => logger.error(s"Failed to deploy $t in ${timeStat(ts)}, \t${ex.getMessage}")
              case Success(_) => logger.info(s"Deployed $t in ${timeStat(ts)}")
            }
          }
          case CFGFailure(x) => logger.warn(s"Failed to deploy verticle from configuration",x.configException)
        }

        vLauncher.success(v)
      } else {
        logger.info("Failed to initialize Vertx cluster")
        vLauncher.failure(evt.cause())
      }
    })
    clusterManager.join((res: AsyncResult[Void]) => {
      def join(res: AsyncResult[Void]) = if (res.succeeded) logger.info("Joined cluster")

      join(res)
    })

    vLauncher.future
  }

  private def toBilliSeconds(l: Long) = {
    l * 1000 * 1000000
  }
}
