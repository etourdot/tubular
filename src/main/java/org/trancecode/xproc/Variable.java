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
import org.trancecode.core.TubularObjects;
import org.trancecode.xml.AbstractHasLocation;
import org.trancecode.xml.Location;

import net.sf.saxon.s9api.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class Variable extends AbstractHasLocation
{
	private static final Logger LOG = LoggerFactory.getLogger(Variable.class);


	private static enum Type
	{
		OPTION, VARIABLE, PARAMETER
	};

	private final QName name;
	private final String select;
	private final String value;
	private final Boolean required;
	private final Type type;
	private final PortBinding portBinding;


	public static Variable newOption(final QName name)
	{
		return new Variable(name, null, null, null, null, Type.OPTION, null);
	}


	public static Variable newParameter(final QName name)
	{
		return new Variable(name, null, null, null, null, Type.PARAMETER, null);
	}


	public static Variable newVariable(final QName name)
	{
		return new Variable(name, null, null, null, null, Type.VARIABLE, null);
	}


	public static Variable newOption(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.OPTION, null);
	}


	public static Variable newParameter(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.PARAMETER, null);
	}


	public static Variable newVariable(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.VARIABLE, null);
	}


	private Variable(
		final QName name, final String select, final String value, final Boolean required, final Location location,
		final Type type, final PortBinding portBinding)
	{
		super(location);

		assert name != null;
		this.name = name;
		this.select = select;
		this.value = value;
		this.required = required;
		this.type = type;
		this.portBinding = portBinding;
	}


	public Variable setLocation(final Location location)
	{
		return new Variable(name, select, value, required, location, type, portBinding);
	}


	public boolean isVariable()
	{
		return type == Type.VARIABLE;
	}


	public boolean isParameter()
	{
		return type == Type.PARAMETER;
	}


	public boolean isOption()
	{
		return type == Type.OPTION;
	}


	public QName getName()
	{
		return name;
	}


	public String getSelect()
	{
		return select;
	}


	public Variable setSelect(final String select)
	{
		return new Variable(name, select, value, required, location, type, portBinding);
	}


	public String getValue()
	{
		return value;
	}


	public Variable setValue(final String value)
	{
		return new Variable(name, select, value, required, location, type, portBinding);
	}


	public Variable setRequired(final boolean required)
	{
		return new Variable(name, select, value, required, location, type, portBinding);
	}


	public boolean isRequired()
	{
		return required != null && required;
	}


	public boolean isNotRequired()
	{
		return required != null && !required;
	}


	@Override
	public String toString()
	{
		return String.format("%s[%s]%s%s", type, name, TubularObjects.conditional(select != null, "[select=" + select
			+ "]", ""), TubularObjects.conditional(value != null, "[value=" + value + "]", ""));
	}


	@ReturnsNullable
	public PortBinding getPortBinding()
	{
		return portBinding;
	}


	public Variable setPortBinding(final PortBinding portBinding)
	{
		LOG.trace("{}", name);
		LOG.trace("{} -> {}", this.portBinding, portBinding);
		return new Variable(name, select, value, required, location, type, portBinding);
	}
}
