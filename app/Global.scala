import library.Redis
import play.api
import play.api.GlobalSettings

/*
* Copyright (c) 2012. Nicolas Martignole
*
* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
* If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/.
*/

/**
 * Global Play application
 *
 * Author: nicolas
 * Created: 04/08/2012 17:48
 */

object Global extends GlobalSettings {
  override def onStop(app: api.Application) {
    super.onStop(app)
    play.Logger.info("Stopping redxplay...")
    Redis.disconnect()
  }
}
