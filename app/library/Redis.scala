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

case class RedisSession(hostname: String, port: Int, auth: Option[String])

object Redis {

  private[this] var _pool: Pool = null
  private[this] var _hostname: Option[String] = None
  private[this] var _auth: Option[String] = None
  private[this] var _port: Option[Int] = None

  def connectTo(hostname: String, port: Int, auth: Option[String]): Either[String, String] = {
    // First disconnect, if we were connected
    disconnect()
    // then recreate a new JedisPool
    val pool = new Pool(new JedisPool(new JedisPoolConfig(), hostname, port, 60000, auth.getOrElse(null)))
    try {
      pool.withClient {
        client =>
          client.ping()
          this._hostname = Option(hostname)
          this._port = Option(port)
      }
      _pool = pool
    } catch {
      case e: Exception => "Unable to connect to " + hostname + ":" + port + " due to " + e.getMessage
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
      _hostname = None
      _port = None
      play.Logger.info("Disconnected from Redis ")
    }
  }

  def isConnected: Boolean = {
    _pool != null
  }

  def getInfo: Option[String] = {
    if (_pool != null) {
      _pool.withClient {
        client =>
          Option(client.info())
      }
    } else {
      None
    }
  }

  def pool = _pool

  def currentRedisSession: Option[RedisSession] = {
    val res = for (hostname <- _hostname.toRight("No hostmame").right;
                   port <- _port.toRight("No port").right) yield RedisSession(hostname, port, _auth)
    res.fold(error => {
      play.Logger.error(error); None
    }, validSession => Some(validSession))
  }
}
