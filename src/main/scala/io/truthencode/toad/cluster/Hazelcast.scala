package io.truthencode.toad.cluster

import com.hazelcast.config.Config

/**
  * Created by adarr on 7/7/2016.
  */
object Hazelcast {
val config =new Config()
  config.setProperty( "hazelcast.logging.type","slf4j")


}
