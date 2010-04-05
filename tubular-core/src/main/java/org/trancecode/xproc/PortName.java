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
 */
public final class PortName
{
    private final String name;
    private int hashCode;

    public static PortName newInstance(final String name)
    {
        return new PortName(name);
    }

    private PortName(final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        if (hashCode == 0)
        {
            hashCode = new HashCodeBuilder(15, 17).append(name).toHashCode();
        }

        return hashCode;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o != null && o instanceof PortName)
        {
            final PortName portName = (PortName) o;
            return new EqualsBuilder().append(name, portName.name).isEquals();
        }

        return false;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
