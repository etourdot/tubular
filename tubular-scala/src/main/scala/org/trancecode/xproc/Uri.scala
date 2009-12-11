/*
 * Copyright 2009 TranceCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.trancecode.xproc

import java.net.URI

object Uri {

  def resolve(href: Option[String], base: Option[String]): Option[String] = {
    (href, base) match {
      case (None, _) => base
      case (_, None) => href
      case _ => Some(URI.create(base.get).resolve(href.get).toString())
    }
  }

}
