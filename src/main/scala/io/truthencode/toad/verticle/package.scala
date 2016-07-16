package io.truthencode.toad


import _root_.io.vertx.core.Handler
import io.vertx.core.json.JsonObject



/**
  * The verticle package contains VertX related utilities and Verticles as well as any Vertx specific implicits.
  */
package object verticle {

  /**
    * Vert.x specific implicits mostly used for Java / Scala compatibility.
    */
  object Event2HandlerImplicits {

    import scala.language.implicitConversions

    /**
      * Maps java events to scala handlers used mostly to transliterate Vert.x Java lambdas to Scala.
      *
      * @example
      * {{{
      *  // Java
      *  Vertx.clusteredVertx(options, evt -> {
      *   if (evt.succeeded()) //do something with vertx
      *       Vertx vertx = evt.result();
      *   else
      *       log.error("insightful message",res.cause());
      *  }
      *  // Scala
      *  import io.truthencode.toad.verticle.Event2HandlerImplicits._
      *  Vertx.clusteredVertx(options, (evt: AsyncResult[Vertx]) => {
      *   if (evt.succeeded()) // do something with vertx
      *    val v = evt.result()
      *   else
      *     log.error("insightful message",res.cause)
      *    }
      *  }}}
      * @note also works where for AsyncResult[Unit]
      * @param event function to fire
      * @tparam T generally a parameterized asynchronous event
      * @tparam S payload of the T
      * @return a mapped handler
      *
      */
    implicit def asyncToHandler[T, S](event: (T) => S): Handler[T] = new Handler[T] {
      override def handle(dEvent: T): Unit = event(dEvent)
    }

    implicit def jsonToString(j: JsonObject): String = j.encodePrettily()
  }

}
