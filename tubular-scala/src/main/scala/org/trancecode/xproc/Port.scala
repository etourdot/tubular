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

import net.sf.saxon.s9api.XdmNode

case class Port(reference: PortReference, primary: Boolean, bindings: List[PortBinding]) {

  def readNodes(environment: Environment): List[XdmNode] = {
    List.flatten(bindings.map(_.readNodes(environment)))
  }

  def readNode(environment: Environment): Option[XdmNode] = {
    val nodes = readNodes(environment)
    nodes.size match {
      case 0 => None
      case 1 => Some(nodes(0))
      case _ => throw new IllegalStateException(nodes.size.toString)
    }
  }

  def << (node: XdmNode): Port = {
    new Port(reference, primary, bindings ::: List(new InlinePortBinding(node)))
  }

}
