package io.truthencode.toad.cluster

import com.hazelcast.config.{Config, ListenerConfig}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager

/**
  * Created by adarr on 7/7/2016.
  */
object Hazelcast extends LazyLogging {

  val clusterManager: HazelcastClusterManager = defaultManager()

  private def defaultManager() = {
    val hcm = new HazelcastClusterManager()
    hcm.setConfig(genConfig())
    hcm
  }


  private def genConfig() = {
    val cfg = ConfigFactory.load("hazelcast_server.conf")
    val c = new Config()
    c.addListenerConfig(
      new ListenerConfig("io.truthencode.toad.cluster.ClusterMembershipListener"))
    cfg.root().entrySet().forEach { e =>
      val (k, v) = (e.getKey, e.getValue)
      logger.debug(s"Extracting config: $k:$v")
      c.setProperty(k, v.render())
    }
    c
  }

}
