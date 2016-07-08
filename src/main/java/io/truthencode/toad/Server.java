package io.truthencode.toad;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import io.truthencode.toad.verticle.MyFirstVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Bootstrapping for Java Services
 * Created by Andre White on 7/2/2016.
 */
class Server {
  //Private constructor
  private Server() {
  }

  private static Server server;

  public synchronized static Server getServer() {
    if (server == null) {
      server = new Server();
    }
    return server;
  }

  private final Logger log = LoggerFactory.getLogger(Server.class);

  private void initVertxCluster() {
    HazelcastClusterManager clusterManager = new HazelcastClusterManager();

    VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        EventBus eventBus = vertx.eventBus();
        eventBus.start(resp -> {
          if (resp.succeeded()) {
            log.info("Initialized Clustered Vertx eventBus");
            DeploymentOptions opts = new DeploymentOptions();

            vertx.deployVerticle(new MyFirstVerticle(), opts);
          } else {
            log.error("Failed to initialize Clustered Vertx eventBus", resp.cause());
          }
        });
        eventBus.close(ebRes -> {
          if (ebRes.succeeded()) {
            log.info("Closing Clustered Vertx Eventbus");
          } else {
            log.error("Failed to close Clustered Vertx Eventbus", ebRes.cause());
          }
        });
      } else {
        log.error("Failed to Initialize Vertx Cluster: " , res.cause());
      }
    });

//    Cluster cluster = clusterManager.getHazelcastInstance().getCluster();
//    cluster.addMembershipListener(new ClusterMembershipListener());
    clusterManager.join(res -> {
      if (res.succeeded()) {
        log.info("Joined cluster");
      }
    });
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutting down Hazelcast");
      HazelcastInstance instance = clusterManager.getHazelcastInstance();
      if (instance.getPartitionService().isClusterSafe()) {
        IExecutorService svc = instance.getExecutorService(Server.class.getName());
        svc.executeOnAllMembers(new ShutdownMember());
      }
    }));
  }

  private void StartVertX() {
    initVertxCluster();
  }

  static void Start() {
    Server.getServer().StartVertX();
  }

  private static class ShutdownMember implements Runnable, HazelcastInstanceAware, Serializable {

    private HazelcastInstance node;

    @Override
    public void run() {
      node.getLifecycleService().shutdown();
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance node) {
      this.node = node;
    }
  }
}
