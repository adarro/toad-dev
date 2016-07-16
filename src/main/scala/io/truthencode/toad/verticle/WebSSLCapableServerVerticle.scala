package io.truthencode.toad.verticle

import java.io.{FileOutputStream, IOException}
import java.security._
import java.security.cert.{Certificate, CertificateException}
import java.util.Date

import com.typesafe.scalalogging.slf4j.LazyLogging
import io.truthencode.toad.verticle.Event2HandlerImplicits._
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.net._
import io.vertx.core.{AbstractVerticle, AsyncResult, Future, Handler}
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.shiro.{ShiroAuth, ShiroAuthOptions, ShiroAuthRealmType}
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.{BridgeOptions, PermittedOptions, SockJSHandler}
import sun.security.tools.keytool.CertAndKeyGen
import sun.security.x509.X500Name



/**
  * A [[io.vertx.core.Verticle]] which starts up the HTTP server for the web application UI. Based on the given
  * configuration, the web server may be configured for SSL using a self-generated SSL cert or a provided SSL certificate
  * file. The application accepts P12, PEM, and JKS files.
  *
  * The web server also configures handlers for the Auth Service and the [[io.vertx.ext.web.handler.sockjs.SockJSHandler]]event bus bridge.
  *
  * @author <a href="https://github.com/InfoSec812">Deven Phillips</a>
  */
class WebSSLCapableServerVerticle extends AbstractVerticle with LazyLogging {

  /**
    * Start this [[io.vertx.core.Verticle]]asynchronously and notify the deploying verticle on success or failure
    *
    * @param startFuture A [[java.util.concurrent.Future]] with which to notify the deploying [[io.vertx.core.Verticle]] about success or failure
    * @throws Exception If there is an uncaught error.
    */
  @throws[Exception]
  override def start(startFuture: Future[Void]) = {
    val config = context.config().getJsonObject("webserver")
    val authCfg = context.config().getJsonObject("authentication")

    val bindAddress = config.getString("bind-address", "0.0.0.0")
    val bindPort = config.getInteger("bind-port", 8080)
    val webRoot: String = config.getString("webroot", "webroot/")

    val router = Router.router(vertx)
    val sockjs = SockJSHandler.create(vertx)
    val opts: BridgeOptions = new BridgeOptions()
      // Allow inbound events going to the terminal session
      .addInboundPermitted(new PermittedOptions()
      .setAddressRegex("^terminal\\.to\\.server\\..*$"))
      .addInboundPermitted(new PermittedOptions()
        .setAddressRegex("^delegate\\.control\\..*$")
        .setRequiredAuthority("session"))
      // Allow inbound events revoking control from invited collaborator
      .addInboundPermitted(new PermittedOptions()
      .setAddressRegex("^revoke\\.control\\..*$")
      .setRequiredAuthority("session"))
      // Allow inbound events for chat messages coming from the client for a given session
      .addInboundPermitted(new PermittedOptions()
      .setAddressRegex("^chat\\.to\\.server\\..*$")
      .setRequiredAuthority("session"))
      // Allow inbound events requesting control of a shared terminal
      .addInboundPermitted(new PermittedOptions()
      .setAddressRegex("^request\\.control\\..*$")
      .setRequiredAuthority("session"))
      // Allow inbound events releasing control of a shared terminal
      .addInboundPermitted(new PermittedOptions()
      .setAddressRegex("^release\\.control\\..*$")
      .setRequiredAuthority("session"))
      // Allow outbound events from the terminal session to the UI
      .addOutboundPermitted(new PermittedOptions()
      .setAddressRegex("^server\\.to\\.terminal\\..*$")
      .setRequiredAuthority("session"))
      // Allow outbound events from the server to participants in a session's chat
      .addOutboundPermitted(new PermittedOptions()
      .setAddressRegex("^server\\.to\\.chat\\..*$")
      .setRequiredAuthority("session"))
      // Allow outbound events to the UI indicating that control was granted
      .addOutboundPermitted(new PermittedOptions()
      .setAddressRegex("^control\\.granted\\..*$")
      .setRequiredAuthority("session"))
      // Allow outbound events to the UI indicating that control was revoked
      .addOutboundPermitted(new PermittedOptions()
      .setAddressRegex("^control\\.revoked\\..*$")
      .setRequiredAuthority("session"))
    sockjs.bridge(opts)

    var authProvider: AuthProvider = null

    if (authCfg.containsKey("auth-provider")) {
      authCfg.getString("auth-provider") match {
        case "ldap" =>
          val opts = new ShiroAuthOptions().setType(ShiroAuthRealmType.LDAP).setConfig(authCfg)
          authProvider = ShiroAuth.create(vertx, opts);
        case "jdbc" =>

        case _ =>
      }
    }

    router.route("/eventbus/*").handler(sockjs)

    router.route().handler(StaticHandler.create(webRoot).setIndexPage("index.html"))
    //    import java.util.function._
    //    import scala.compat.java8.FunctionConverters._
    // If SSL is requested, prepare the SSL configuration off of the event bus to prevent blocking.
    if (config.containsKey("ssl") && config.getBoolean("ssl")) {
      val fut = new Handler[Future[HttpServerOptions]] {
        override def handle(future: Future[HttpServerOptions]) = {
          val httpOpts = new HttpServerOptions()
          if (config.containsKey("certificate-path")) {
            val certPath = config.getString("certificate-path")
            // Use a Java Keystore File
            if (certPath.toLowerCase().endsWith("jks") && config.getString("certificate-password") != null) {
              httpOpts.setKeyStoreOptions(new JksOptions()
                .setPassword(config.getString("certificate-password"))
                .setPath(certPath))
              httpOpts.setSsl(true)

              // Use a PKCS12 keystore
            } else if (config.getString("certificate-password") != null &&
              certPath.matches("^.*\\.(pfx|p12|PFX|P12)$")) {
              httpOpts.setPfxKeyCertOptions(new PfxOptions()
                .setPassword(config.getString("certificate-password"))
                .setPath(certPath))
              httpOpts.setSsl(true)

              // Use a PEM key/cert pair
            } else if (certPath.matches("^.*\\.(pem|PEM)$")) {
              httpOpts.setPemKeyCertOptions(new PemKeyCertOptions()
                .setCertPath(certPath)
                .setKeyPath(certPath))
              httpOpts.setSsl(true)
            } else {
              startFuture.fail("A certificate file was provided, but a password for that file was not.")
            }
          } else try {
            // Generate a self-signed key pair and certificate.
            val store = KeyStore.getInstance("JKS")
            store.load(null, null)
            val keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null)
            val x500Name = new X500Name("localhost", "IT", "unknown", "unknown", "unknown", "unknown")
            keypair.generate(1024)
            val privKey = keypair.getPrivateKey
            val chain = new Array[Certificate](1) //( 1) ;
            val cert: Certificate = keypair.getSelfCertificate(x500Name, new Date(), 365 * 24 * 60 * 60)
            chain(0) = cert
            store.setKeyEntry("selfsigned", privKey, "changeit".toCharArray, chain)
            store.store(new FileOutputStream(".keystore"), "changeit".toCharArray)
            httpOpts.setKeyStoreOptions(new JksOptions().setPath(".keystore").setPassword("changeit"))
            httpOpts.setSsl(true);
          } catch {
            case ex@(_: KeyStoreException | _: IOException | _: NoSuchAlgorithmException | _: CertificateException | _: NoSuchProviderException | _: InvalidKeyException | _: SignatureException) =>
              logger.error("Failed to generate a self-signed cert and other SSL configuration methods failed.", ex)
              startFuture.fail(ex)
          }
          future.complete(httpOpts)
        }
      }
      val rslt: Handler[AsyncResult[HttpServerOptions]] = (result: AsyncResult[HttpServerOptions]) => {
        if (!result.failed()) {
          vertx.createHttpServer(result.result()).requestHandler(router.accept _).listen(bindPort, bindAddress)
          logger.info("SSL Web server now listening")
          startFuture.complete()
        }
      }

      vertx.executeBlocking(fut, rslt)

    } else {
      // No SSL requested, start a non-SSL HTTP server.
      vertx.createHttpServer().requestHandler(router.accept _).listen(bindPort, bindAddress)
      logger.info("Web server now listening")
      startFuture.complete()
    }
  }
}



