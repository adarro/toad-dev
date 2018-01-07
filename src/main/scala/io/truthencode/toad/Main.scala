package io.truthencode.toad

import com.typesafe.scalalogging.LazyLogging
import io.truthencode.toad.config.Bootstrap._

object Main extends LazyLogging {
  def main(args: Array[String]) {
  //  val (serverIp, serverPort, hostName) = (serverInfo.ip,serverInfo.port,serverInfo.hostName)
    init()
    status()
   // logger.info(s"Configured on ${serverIp} port ${serverPort} @ ${hostName}")
  }
}