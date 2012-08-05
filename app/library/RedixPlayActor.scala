/*
 * Copyright (c) 2012. Nicolas Martignole
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package library

import akka.actor.{Props, Actor}
import play.api.Play.current
import scala.Predef._

import play.libs.Akka

/**
 * Akka actor
 *
 * Author: nicolas
 * Created: 05/08/2012 19:43
 */

case class StartProcess(redisSession:Option[RedisSession])

object RedixPlay {
  val redixActor = Akka.system.actorOf(Props[RedixPlayActor])
}

class RedixPlayActor extends Actor {

  def receive = {
      case StartProcess(redisSession:Option[RedisSession]) => doStartProcess(redisSession)
      case other => play.Logger.error("*** Received an invalid actor message: "+other)
    }

  private def doStartProcess(redisSession:Option[RedisSession])={
    play.Logger.info("RedixPlayActor started with " +redisSession )


  }
}

