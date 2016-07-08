package io.truthencode.toad


import _root_.io.vertx.core.Handler
import io.vertx.core.json.JsonObject

// import shapeless.T

/**
  * Created by adarr on 7/1/2016.
  */
package object verticle {

  object Event2HandlerImplicits {

    import scala.language.implicitConversions

    implicit def asyncToHandler[T, S](event: (T) => S): Handler[T] = new Handler[T] {
      override def handle(dEvent: T): Unit = event(dEvent)
    }

    implicit def jsonToString(j: JsonObject): String = j.encodePrettily()
  }

}
