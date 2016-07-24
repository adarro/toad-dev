package io.truthencode.toad.config

/**
  * Created by adarr on 7/19/2016.
  */
class ConfigurationTest {
  val config = context.config().getJsonObject("webserver")
  val authCfg = context.config().getJsonObject("authentication")
}
