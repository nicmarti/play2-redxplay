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

  // Adapter from a String that is the Redis INFO response to Map[String,String]
  val redisInfoToMap: Enumeratee[String, Map[String, String]] = Enumeratee.mapInput[String] {
    case other =>
      other.map {
        e =>
          try {
            val results = e.split("\r\n")
            val contentParsed = results.map {
              t =>
                val keyVal = t.split(":")
                (keyVal(0), keyVal(1))
            }.toMap
            contentParsed
          } catch {
            case err: Exception => play.Logger.error("Streams error " + err + " with " + other)
            null
          }
      }
  }

  val mapToGeneralInfo: Enumeratee[Map[String, String], JsValue] = Enumeratee.mapInput[Map[String, String]] {
    case someMap =>
      someMap.map {
        contentParsed =>
          Json.toJson(
            Map(
              "event" -> Json.toJson("generalInfo"),
              "uptime_in_days" -> contentParsed.get("uptime_in_days").map(s => Json.toJson(s)).getOrElse(JsNull),
              "redis_version" -> contentParsed.get("redis_version").map(s => Json.toJson(s)).getOrElse(JsNull),
              "uptime_in_seconds" -> contentParsed.get("uptime_in_seconds").map(s => Json.toJson(s)).getOrElse(JsNull)
            )
          )
      }
  }

  val mapToMemoryCpu: Enumeratee[Map[String, String], JsValue] = Enumeratee.mapInput[Map[String, String]] {
    case someMap =>
      someMap.map {
        contentParsed =>
          Json.toJson(
            Map(
              "event" -> Json.toJson("memoryAndCpu"),
              "used_cpu_sys" -> contentParsed.get("used_cpu_sys").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_cpu_user" -> contentParsed.get("used_cpu_user").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_cpu_sys_children" -> contentParsed.get("used_cpu_sys_children").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_cpu_user_children" -> contentParsed.get("used_cpu_user_children").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_memory_human" -> contentParsed.get("used_memory_human").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_memory" -> contentParsed.get("used_memory").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_memory_peak" -> contentParsed.get("used_memory_peak").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_memory_peak_human" -> contentParsed.get("used_memory_peak_human").map(s => Json.toJson(s)).getOrElse(JsNull),
              "used_memory_peak_human" -> contentParsed.get("used_memory_peak_human").map(s => Json.toJson(s)).getOrElse(JsNull),
              "mem_fragmentation_ratio" -> contentParsed.get("mem_fragmentation_ratio").map(s => Json.toJson(s)).getOrElse(JsNull)
            )
          )
      }
  }


  def pollRedis: Enumerator[String] = Enumerator.fromCallback {
    () => Promise.timeout(Redis.getInfo, 5000)
  }

  def pollRedisForMemoryAndCpu: Enumerator[String] = Enumerator.fromCallback {
    () => Promise.timeout(Redis.getInfo, 1000)
  }

  def streamInfo(): Enumerator[JsValue] = {
    pollRedis &> redisInfoToMap &> mapToGeneralInfo
  }

  def streamMemoryAndCpu(): Enumerator[JsValue] = {
    pollRedisForMemoryAndCpu &> redisInfoToMap &> mapToMemoryCpu
  }

  def events(): Enumerator[JsValue] = {
    streamInfo >- streamMemoryAndCpu()
  }
}



