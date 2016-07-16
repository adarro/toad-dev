package io.truthencode.toad.config

/**
  * Main (Http) Server port information used to bind the public facing web interface.
  * @param ip external IP address
  * @param port external Port (may be further mapped by production environment. (Defaults to 8080)
  * @param hostName convenience string for host:port or ip:port.
  */
case class ServerInfo(ip: String, port: String, hostName: String)