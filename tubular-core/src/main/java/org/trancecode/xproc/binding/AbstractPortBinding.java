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
package org.trancecode.xproc.binding;

import java.util.Map;

import org.trancecode.xml.AbstractHasLocation;
import org.trancecode.xml.Location;
import org.trancecode.xproc.port.Port;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractPortBinding extends AbstractHasLocation implements PortBinding
{
    protected AbstractPortBinding(final Location location)
    {
        super(location);
    }

    public PortBinding clonePortBinding(final Map<Port, Port> portMappings)
    {
        return this;
    }
}
