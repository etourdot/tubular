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

case class Step (
  name: String,
  stepType: QName,
  inputPorts: Map[PortReference, Port],
  outputPorts: Map[PortReference, Port],
  variables: Map[QName, Variable],
  function: Function2[Step, Environment, Environment])
  extends Function1[Environment, Environment] {

  def apply(in: Environment): Environment = {

    // TODO setup function environment

    function(this, in)

    // TODO build result environment

  }


  def primaryInputPort: Port = {
    inputPorts.size match {
      case 1 => inputPorts.values.next
      case _ => findPrimary(inputPorts.values.toList)
    }
  }

  def findPrimary(ports: List[Port]): Port = {
    ports.find(_.primary.get).get
  }

}
