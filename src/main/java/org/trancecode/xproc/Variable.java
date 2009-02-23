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

import org.trancecode.log.LoggerHelpers;
import org.trancecode.xml.Location;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Variable extends AbstractHasLocation implements LoggerHelpers
{
	private final QName name;
	private String select;
	private String value;
	private final boolean required;


	public Variable(final QName name, final String select, final boolean required, final Location location)
	{
		super(location);

		assert name != null;
		this.name = name;

		this.select = select;

		this.required = required;
	}


	public QName getName()
	{
		return name;
	}


	public String getSelect()
	{
		return select;
	}


	public void setSelect(final String select)
	{
		assert select != null;
		assert select.length() > 0;

		this.select = select;
	}


	public String getValue()
	{
		return value;
	}


	public void setValue(final String value)
	{
		assert value != null;

		this.value = value;
	}


	public boolean isRequired()
	{
		return required;
	}


	@Override
	public String toString()
	{
		return String
			.format("%s name = %s ; select = %s ; value = %s", getClass().getSimpleName(), name, select, value);
	}
}
