package io.truthencode.toad.config

import com.typesafe.scalalogging.slf4j.LazyLogging
import configs.Result.{Failure, Success}
import configs.syntax.ConfigOps

/**
  * Created by adarr on 7/15/2016.
  */
case class DbInfo(host: String, port: Int, uuid: String, userId: String, pwd: Array[Char], url: String)

object DbInfo extends LazyLogging {
  def apply(host: String, port: Int, uuid: String, userId: String, pwd: String, url: String): DbInfo = DbInfo(host, port, uuid, userId, pwd.toCharArray, url)

  def apply: DbInfo = {
    val cfg = if (config.isResolved) config else config.resolve
    cfg.get[DbInfo]("db-info") match {
      case Success(x) => DbInfo(x.host, x.port, x.uuid, x.userId, x.pwd, x.url)
      case Failure(x) =>
        val msg = "Error reading Server configuration"
        logger.error(msg, x.configException)
        throw x.configException(msg)
    }
  }
}
