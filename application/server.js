var Context = require("vertx-js/context");
var context = vertx.getOrCreateContext();
if (context.isEventLoopContext()) {
  console.log("Context attached to Event Loop");
} else if (context.isWorkerContext()) {
  console.log("Context attached to Worker Thread");
} else if (context.isMultiThreadedWorkerContext()) {
  console.log("Context attached to Worker Thread - multi threaded worker");
} else if (!Context.isOnVertxThread()) {
  console.log("Context not attached to a thread managed by vert.x");
}

var eb = vertx.eventBus();
var consumer = eb.consumer("news.uk.sport");
consumer.completionHandler(function (res, res_err) {
  if (res_err == null) {
    console.log("The handler registration has reached all nodes");
  } else {
    console.log("Registration failed!");
  }
});

eventBus.publish("news.uk.sport", "Yay! Someone kicked a ball");