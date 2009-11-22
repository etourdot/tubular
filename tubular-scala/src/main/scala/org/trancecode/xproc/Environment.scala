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

import scala.collection.immutable.Map
import scala.xml._

case class Environment (
  ports: Map[PortReference, Port],
  variables: Map[QName, String],
  defaultInputPort: Option[Port],
  defaultXPathContextPort: Option[Port],
  configuration: Configuration) {

  val emptyContextNode: Node = <document/>

  def xpathContextPort: Option[Port] = {
    if (!defaultXPathContextPort.isEmpty)
      defaultXPathContextPort
    else
      defaultInputPort
  }

  def xpathContextNode: Node = {
    val port = xpathContextPort
    if (port.isEmpty)
      emptyContextNode
    else
      port.get.readNode(this).get
  }

  def evaluateXPath(query: String): String = {
    evaluateXPath(query, variables, xpathContextNode)
  }

  def evaluateXPath(query: String, variables: Map[QName, String], contextNode: Node): String = {
    // TODO
    "TODO"
  }

}
