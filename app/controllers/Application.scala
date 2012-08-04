/*
 * Copyright (c) 2012. Nicolas Martignole
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play
import play.api.data.validation.Constraints
import play.api.Play.current

case class RedisParameters(hostname: String, port: Long, auth: Option[String])

object Application extends Controller {

  val paramsForm = Form(
    mapping(
      "hostname" -> text.verifying(Constraints.nonEmpty),
      "port" -> longNumber,
      "auth" -> optional(text)
  )(RedisParameters.apply)(RedisParameters.unapply))

  def index = Action {
    val hostname = Play.configuration.getString("redis.hostname").getOrElse("localhost")
    val port = Play.configuration.getInt("redis.port").getOrElse(6379)
    val auth = Play.configuration.getString("redis.auth")
    Ok(views.html.index(paramsForm.fill(RedisParameters(hostname, port, auth))))
  }

  def startSession = Action{
    TODO
  }
}