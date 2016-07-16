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
  /**
   * Private Constructor Server creates a new Server instance.
   */
  private Server() {
  }

  private static Server server;

  /**
   * Retrieves a singleton Server instance.
   *
   * @return the server (type Server) of this Server object.
   */
  private synchronized static Server getServer() {
    if (server == null) {
      server = new Server();
    }
    return server;
  }

  private final Logger log = LoggerFactory.getLogger(Server.class);

  /**
   * Initializes Vertx in clustered a environment.
   */
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
        log.error("Failed to Initialize Vertx Cluster: ", res.cause());
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

  /**
   * Internally calls {@link #initVertxCluster()}
   */
  private void StartVertX() {
    initVertxCluster();
  }

  /**
   * Starts the Vertx server.
   *
   * @implNote Internally calls {@link #initVertxCluster()} which does not use the Bootstrap to start the cluster.
   */
  static void Start() {
    Server.getServer().StartVertX();
  }

  /**
   * Internal utility class to handle shutting down Hazelcast instances
   */
  private static class ShutdownMember implements Runnable, HazelcastInstanceAware, Serializable {
    /** Field serialVersionUID @see {@link Serializable}  */
    static final long serialVersionUID = 42L;
    private HazelcastInstance node;

    /**
     * Calls shutdown on the current instance node.
     */
    @Override
    public void run() {
      node.getLifecycleService().shutdown();
    }

    /**
     * Sets the node to the current instance.
     *
     * @param node The Current (live) node.
     */
    @Override
    public void setHazelcastInstance(HazelcastInstance node) {
      this.node = node;
    }
  }
}
