package me.zup.simpleEndpoint

import com.typesafe.config.ConfigFactory

object AppConfig {
  private val config = ConfigFactory.load()
  private val serviceConfig = config.getConfig("app")
  lazy val interface = serviceConfig.getString("interface")
  lazy val port = serviceConfig.getInt("port")
  lazy val rootDataDir = serviceConfig.getString("root_data_dir")
  lazy val filesConfig = serviceConfig.getConfig("files")
  lazy val small = filesConfig.getInt("small")
  lazy val medium = filesConfig.getInt("medium")
  lazy val big = filesConfig.getInt("big")
  lazy val large = filesConfig.getInt("large")
}
