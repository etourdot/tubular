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
import org.trancecode.log.Logger;
import org.trancecode.log.LoggerHelpers;
import org.trancecode.log.LoggerManager;
import org.trancecode.xml.Location;
import org.trancecode.xml.SaxonUtil;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPortBinding;
import org.trancecode.xproc.PortBinding;

import java.util.Collections;

import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
@Immutable
public class InlinePortBinding extends AbstractPortBinding implements PortBinding, LoggerHelpers
{
	private final Logger log = LoggerManager.getLogger(this);
	private final XdmNode node;


	public InlinePortBinding(final XdmNode node, final Location location)
	{
		super(location);

		assert node != null;
		this.node = node;
	}


	@Override
	public EnvironmentPortBinding newEnvironmentPortBinding(final Environment environment)
	{
		return new AbstractBoundPortBinding(location)
		{
			public Iterable<XdmNode> readNodes()
			{
				log.trace("%s node = %s", METHOD_NAME, SaxonUtil.nodesToString(node));
				log.trace("%s node = %s", METHOD_NAME, node);
				return Collections.singletonList(node);
			}
		};
	}


	@Override
	public String toString()
	{
		return String.format("%s[%s]", getClass().getSimpleName(), node.getNodeName());
	}
}
