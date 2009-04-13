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

import org.trancecode.xml.Location;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class Variable extends AbstractHasLocation
{
	private static enum Type
	{
		OPTION, VARIABLE, PARAMETER
	};

	private final QName name;
	private final String select;
	private final String value;
	private final Boolean required;
	private final Type type;


	public static Variable newOption(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.OPTION);
	}


	public static Variable newParameter(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.PARAMETER);
	}


	public static Variable newVariable(final QName name, final Location location)
	{
		return new Variable(name, null, null, null, location, Type.VARIABLE);
	}


	private Variable(
		final QName name, final String select, final String value, final Boolean required, final Location location,
		final Type type)
	{
		super(location);

		assert name != null;
		this.name = name;
		this.select = select;
		this.value = value;
		this.required = required;
		this.type = type;
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
		return new Variable(name, select, value, required, location, type);
	}


	public String getValue()
	{
		return value;
	}


	public Variable setValue(final String value)
	{
		return new Variable(name, select, value, required, location, type);
	}


	public Variable setRequired(final boolean required)
	{
		return new Variable(name, select, value, required, location, type);
	}


	public boolean isRequired()
	{
		return required != null && required;
	}


	public boolean isNotRequired()
	{
		return required != null && !required;
	}


	public String evaluate(final Environment environment)
	{
		if (value != null)
		{
			return value;
		}

		if (select == null)
		{
			if (isRequired())
			{
				throw new PipelineException("Unbound variable %s @ %s", getName(), getLocation());
			}

			return null;
		}
		else
		{
			try
			{
				return environment.evaluateXPath(select).toString();
			}
			catch (final Exception e)
			{
				throw new PipelineException(e, "error while evaluating variable $%s ; select = %s", getName(), select);
			}
		}
	}


	@Override
	public String toString()
	{
		return String
			.format("%s name = %s ; select = %s ; value = %s", getClass().getSimpleName(), name, select, value);
	}
}
