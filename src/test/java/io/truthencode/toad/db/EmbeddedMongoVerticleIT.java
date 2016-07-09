package io.truthencode.toad.db;

import io.vertx.core.VertxOptions;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EmbeddedMongoVerticleIT extends VertxTestBase {

  @Override
  public VertxOptions getOptions() {
    // It can take some time to download the first time!
    return new VertxOptions().setMaxWorkerExecuteTime(30 * 60 * 1000);
  }

  @Test
  public void testEmbeddedMongo() {
    // Not really sure what to test here apart from start and stop
    vertx.deployVerticle("service:io.vertx.vertx-mongo-embedded-db", onSuccess(deploymentID -> {
      assertNotNull(deploymentID);
      vertx.undeploy(deploymentID, onSuccess(v -> {
        assertNull(v);
        testComplete();
      }));
    }));
    await();
  }
}
