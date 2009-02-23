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

import org.trancecode.log.Logger;
import org.trancecode.log.LoggerHelpers;
import org.trancecode.log.LoggerManager;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPort;
import org.trancecode.xproc.EnvironmentPortBinding;
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.PortReference;

import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class PipePortBinding extends AbstractPortBinding implements PortBinding, LoggerHelpers
{
	private final Logger log = LoggerManager.getLogger(this);

	private final PortReference portReference;


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

		return new AbstractBoundPortBinding(location)
		{
			public Iterable<XdmNode> readNodes()
			{
				log.trace("%s boundPort = %s", METHOD_NAME, boundPort);
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
