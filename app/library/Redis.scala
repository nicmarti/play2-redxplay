/*
 * Copyright (c) 2012. Nicolas Martignole
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package library

import org.sedis.Pool
import redis.clients.jedis.{JedisPoolConfig, JedisPool}
import redis.clients.jedis.exceptions.JedisConnectionException

/**
 * TODO definition
 *
 * Author: nicolas
 * Created: 04/08/2012 17:13
 */

object Redis {

  private[this] var _pool: Pool = null

  def connectTo(hostname: String, port: Int, auth: Option[String]): Either[String, String] = {
    // First disconnect, if we were connected
    disconnect()
    // then recreate a new JedisPool
    val pool = new Pool(new JedisPool(new JedisPoolConfig(), hostname, port, 30000, auth.getOrElse(null)))
    try {
      pool.withClient {
        client =>
          client.ping()
      }
      _pool = pool
    } catch {
      case ctx: JedisConnectionException => "Unable to connect to " + hostname + ":" + port
    }

    val toReturn: Either[String, String] = _pool match {
      case null => Left("Error, could not connect on " + hostname + ":" + port)
      case _ => Right("Connected on " + hostname + ":" + port)
    }
    toReturn
  }

  def disconnect() = {
    if (_pool != null) {
      // Send a disconnect to the server
      _pool.withClient {
        client => client.quit()
      }
      // close the pool
      _pool.underlying.destroy()
      _pool = null
      play.Logger.info("Disconnected from Redis " + _pool)
    }
  }

  def isConnected: Boolean = {
    _pool != null
  }

  def getInfo: Option[String] = {
    if (_pool!=null) {
      _pool.withClient {
        client =>
          Option(client.info())
      }
    } else {
      None
    }
  }

  def pool = _pool

}
