/*
 * Copyright (C) 2008 Herve Quiroz
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
package org.trancecode.xproc.port;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Herve Quiroz
 */
public class PortReference
{
    private final String stepName;
    private final String portName;
    private transient int hashCode;
    private transient String toString;

    public static PortReference newReference(final String step, final String port)
    {
        return new PortReference(step, port);
    }

    private PortReference(final String step, final String portName)
    {
        Preconditions.checkArgument(step == null || !step.isEmpty());
        this.stepName = step;

        Preconditions.checkNotNull(portName);
        Preconditions.checkArgument(!portName.isEmpty());
        this.portName = portName;
    }

    public String getStepName()
    {
        return stepName;
    }

    public String getPortName()
    {
        return portName;
    }

    public PortReference setStepName(final String stepName)
    {
        return new PortReference(stepName, portName);
    }

    @Override
    public int hashCode()
    {
        if (hashCode == 0)
        {
            hashCode = new HashCodeBuilder(7, 13).append(stepName).append(portName).toHashCode();
        }

        return hashCode;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o != null && o instanceof PortReference)
        {
            final PortReference portReference = (PortReference) o;
            return new EqualsBuilder().append(stepName, portReference.stepName).append(portName, portReference.portName).isEquals();
        }

        return false;
    }

    @Override
    public String toString()
    {
        if (toString == null)
        {
            toString = stepName + "/" + portName;
        }

        return toString;
    }
}
