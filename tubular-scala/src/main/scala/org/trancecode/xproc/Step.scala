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
import net.sf.saxon.s9api._

case class Step (
  name: String,
  stepType: QName,
  inputPorts: Map[PortReference, Port],
  outputPorts: Map[PortReference, Port],
  variables: Map[QName, Variable],
  function: Function2[Step, Environment, Environment])
  extends Function1[Environment, Environment] {

  def apply(environment: Environment): Environment = {

    // TODO setup function environment

    val workingEnvironment = setupVariables(setupPorts(environment))

    function(this, workingEnvironment)

    // TODO build result environment

  }

  def setupVariables(environment: Environment) = {
    // TODO
    environment
  }

  def setupPorts(environment: Environment) = {
    val stepInputPorts = environment.ports ++ inputPorts
    val defaultReadablePort = {
      if (primaryInputPort.isDefined)
        primaryInputPort
      else
        environment.defaultInputPort
    }

    // TODO setup XPath context port

    (environment ++ inputPorts).withDefaultInputPort(defaultReadablePort)
  }


  def evaluateVariables(environment: Environment): Map[QName, String] = {
    variables.transform((name: QName, variable: Variable) => variable(environment))
  }


  def primaryInputPort: Option[Port] = primaryPort(inputPorts.values.toList)

  def primaryOutputPort: Option[Port] = primaryPort(outputPorts.values.toList)

  def primaryPort(ports: List[Port]): Option[Port] = {
    ports.length match {
      case 1 => Some(ports(0))
      case _ => ports.find(_.primary.get)
    }
  }

}
