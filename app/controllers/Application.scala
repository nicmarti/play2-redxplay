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
import library.{StartProcess, RedixPlay, Streams, Redis}
import play.api.libs.EventSource.EventNameExtractor
import play.api.libs.json.JsValue
import play.api.libs.EventSource

case class RedisParameters(hostname: String, port: Int, auth: Option[String])

object Application extends Controller {

  val paramsForm = Form(
    mapping(
      "hostname" -> text.verifying(Constraints.nonEmpty),
      "port" -> number,
      "auth" -> optional(text)
    )(RedisParameters.apply)(RedisParameters.unapply))

  def index = Action {
    implicit request =>
      val hostname = Play.configuration.getString("redis.hostname").getOrElse("localhost")
      val port = Play.configuration.getInt("redis.port").getOrElse(6379)
      val auth = Play.configuration.getString("redis.auth")
      Ok(views.html.index(paramsForm.fill(RedisParameters(hostname, port, auth))))
  }

  def startSession = Action {
    implicit request =>
      paramsForm.bindFromRequest.fold(
        errors => BadRequest(views.html.index(errors)),
        successForm => {
          Redis.connectTo(successForm.hostname, successForm.port, successForm.auth).fold(
            errorCtx => BadRequest(views.html.index(paramsForm)).flashing("error" -> errorCtx),
            successCtx => Redirect(routes.Application.heatmap()).flashing("success" -> successCtx)
          )
        }
      )
  }

  def connected = Action {
    implicit request =>
      if (!Redis.isConnected) {
        Redirect(routes.Application.index()).flashing("error" -> "You are not connected")
      } else {
        Ok(views.html.connected())
      }
  }

  def heatmap = Action {
    implicit request =>
      if (!Redis.isConnected) {
        Redirect(routes.Application.index()).flashing("error" -> "You are not connected")
      } else {
        Ok(views.html.heatmap(Redis.currentRedisSession))
      }
  }

  def startCollect=Action{
    implicit request=>
      RedixPlay.redixActor ! StartProcess(Redis.currentRedisSession)
      Redirect(routes.Application.heatmap()).flashing("success"->"Posted order to compute Redis entries to Akka")
  }

  def disconnect() = Action {
    implicit requet =>
      Redis.disconnect()
      Redirect(routes.Application.index()).flashing("success" -> "Disconnected from Redis server")
  }


  // Streaming using server sent event
  def stream = Action {
    // Define an implicit EventNameExtractor wich extract the "event" name from the Json event so that the EventSource() sets the event in the message
    implicit val eventNameExtractor: EventNameExtractor[JsValue] = EventNameExtractor[JsValue](eventName = (myEvent) => myEvent.\("event").asOpt[String])
    Ok.feed(Streams.events() &> EventSource()).as("text/event-stream")
  }
}