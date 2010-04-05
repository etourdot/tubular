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
package org.trancecode.xproc.binding;

import org.trancecode.annotation.Immutable;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPort;
import org.trancecode.xproc.EnvironmentPortBinding;
import org.trancecode.xproc.PortReference;

import net.sf.saxon.s9api.XdmNode;

/**
 * @author Herve Quiroz
 * @version $Revision$
 */
@Immutable
public class PipePortBinding extends AbstractPortBinding
{
    private final Logger log = Logger.getLogger(getClass());

    private final PortReference portReference;

    public PipePortBinding(final String stepName, final String portName, final Location location)
    {
        this(PortReference.newReference(stepName, portName), location);
    }

    public PipePortBinding(final PortReference portReference, final Location location)
    {
        super(location);

        assert portReference != null;
        this.portReference = portReference;
    }

    @Override
    public EnvironmentPortBinding newEnvironmentPortBinding(final Environment environment)
    {
        final EnvironmentPort boundPort = environment.getEnvironmentPort(portReference);
        assert boundPort != null : "port = " + portReference + " ; location = " + location;

        return new AbstractEnvironmentPortBinding(location)
        {
            public Iterable<XdmNode> readNodes()
            {
                log.trace("{@method} boundPort = {}", boundPort);
                return boundPort.readNodes();
            }
        };
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]", getClass().getSimpleName(), portReference);
    }
}
