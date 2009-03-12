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
import org.trancecode.xml.Location;
import org.trancecode.xml.SaxonUtil;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPortBinding;
import org.trancecode.xproc.PortBinding;

import java.util.Collections;

import net.sf.saxon.s9api.XdmNode;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
@Immutable
public class InlinePortBinding extends AbstractPortBinding implements PortBinding
{
	private final XLogger log = XLoggerFactory.getXLogger(getClass());
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
				log.entry();
				log.trace("node = {}", SaxonUtil.nodesToString(node));
				log.trace("node = {}", node);
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
