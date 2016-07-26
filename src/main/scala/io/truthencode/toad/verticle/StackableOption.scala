package io.truthencode.toad.verticle

import io.vertx.core.DeploymentOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.handler.sockjs.BridgeOptions

/**
  * Created by adarr on 7/25/2016.
  */
trait StackableOption {
  lazy val deploymentOptions: DeploymentOptions = mergeDeployment
  lazy val addressResolverOptions: AddressResolverOptions = mergeAddressResolver
  lazy val httpServerOptions: HttpServerOptions = mergeHttpServer
  lazy val bridgeOptions = mergeBridge


  def mergeDeployment: DeploymentOptions = new DeploymentOptions()

  def mergeHttpServer: HttpServerOptions = new HttpServerOptions()

  def mergeAddressResolver: AddressResolverOptions = new AddressResolverOptions()

  def mergeBridge: BridgeOptions = new BridgeOptions()



}
