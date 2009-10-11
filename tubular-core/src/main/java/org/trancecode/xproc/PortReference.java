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

import com.google.common.base.Preconditions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class PortReference
{
	private final String step;
	private final String port;
	private transient int hashCode;
	private transient String toString;


	public static PortReference newReference(final String step, final String port)
	{
		return new PortReference(step, port);
	}


	private PortReference(final String step, final String port)
	{
		Preconditions.checkArgument(step == null || !step.isEmpty());
		this.step = step;

		Preconditions.checkNotNull(port);
		Preconditions.checkArgument(!port.isEmpty());
		this.port = port;
	}


	public String step()
	{
		return step;
	}


	public String port()
	{
		return port;
	}


	public PortReference setStep(final String step)
	{
		return new PortReference(step, port);
	}


	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = new HashCodeBuilder(7, 13).append(step).append(port).toHashCode();
		}

		return hashCode;
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o != null && o instanceof PortReference)
		{
			final PortReference portReference = (PortReference)o;
			return new EqualsBuilder().append(step, portReference.step).append(port, portReference.port).isEquals();
		}

		return false;
	}


	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = step + "/" + port;
		}

		return toString;
	}
}
