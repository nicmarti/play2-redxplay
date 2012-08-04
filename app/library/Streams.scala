/*
 * Copyright (c) 2012. Nicolas Martignole
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package library

import play.api.libs.json._
import play.api.libs.concurrent.Promise

/**
 * Server sent event stream that retrieve some Redis info then stream back the chunks to the client.
 *
 * Author: nicolas
 * Created: 04/08/2012 18:08
 */

object Streams {

  import play.api.libs.iteratee._

  // Adapter from a String that is the Redis INFO response to JsValue
  val redisInfoToJson: Enumeratee[String, JsValue] = Enumeratee.mapInput[String] {
    case other => {
      other.map {
        e =>
          Json.toJson(
            Map("event" -> Json.toJson("info"),
              "content" -> Json.toJson(e)
            )
          )
      }
    }
  }

  def streamInfo: Enumerator[String] = Enumerator.fromCallback {
    () => Promise.timeout(Redis.getInfo, 2000)
  }

  def events(): Enumerator[JsValue] = {
    streamInfo &> redisInfoToJson
  }
}



