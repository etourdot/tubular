/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 * $Id$
 */
package org.trancecode.xproc;

import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.xml.Location;

import java.util.Map;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface Step extends HasLocation
{
	String getName();


	QName getType();


	Port declarePort(Port port);


	boolean hasOptionDeclared(QName name);


	void declareOption(Option option);


	void declareVariable(Variable variable);


	void withOption(QName name, String select);


	void withOptionValue(QName name, String value);


	void withParam(QName name, String select, String value, Location location);


	Environment run(Environment environment);


	Port getPort(String name);


	Map<String, Port> getPorts();


	void setPortBindings(String portName, PortBinding... portBindings);


	void withPort(Port port);


	Iterable<Port> getInputPorts();


	Iterable<Port> getParameterPorts();


	Iterable<Port> getOutputPorts();


	@ReturnsNullable
	Port getXPathContextPort();


	@ReturnsNullable
	Port getPrimaryInputPort();


	@ReturnsNullable
	Port getPrimaryOutputPort();


	@ReturnsNullable
	Port getPrimaryParameterPort();


	Iterable<Variable> getVariables();
}
