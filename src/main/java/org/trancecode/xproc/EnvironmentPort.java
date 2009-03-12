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

import org.trancecode.xproc.binding.AbstractBoundPortBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class EnvironmentPort
{
	private static final XLogger LOG = XLoggerFactory.getXLogger(EnvironmentPort.class);

	private final Port declaredPort;
	protected final List<EnvironmentPortBinding> portBindings = new ArrayList<EnvironmentPortBinding>();
	private final Processor processor;
	private final XPathExecutable select;


	public EnvironmentPort(final Port declaredPort, final Environment environment)
	{
		assert declaredPort != null;
		this.declaredPort = declaredPort;
		this.processor = environment.getProcessor();

		for (final PortBinding portBinding : declaredPort.getPortBindings())
		{
			portBindings.add(portBinding.newEnvironmentPortBinding(environment));
		}

		final String select = declaredPort.getSelect();
		if (select != null)
		{
			try
			{
				this.select = processor.newXPathCompiler().compile(select);
			}
			catch (final SaxonApiException e)
			{
				throw new PipelineException(e, "error while compiling @select: %s", select);
			}
		}
		else
		{
			this.select = null;
		}

	}


	public Port getDeclaredPort()
	{
		return declaredPort;
	}


	public Iterable<XdmNode> readNodes()
	{
		LOG.entry(declaredPort);

		// TODO improve this by returning a true Iterable
		final List<XdmNode> nodes = Lists.newArrayList();
		for (final EnvironmentPortBinding portBinding : portBindings)
		{
			for (final XdmNode node : portBinding.readNodes())
			{
				if (select != null)
				{
					try
					{
						final XPathSelector selector = select.load();
						selector.setContextItem(node);
						for (final XdmItem xdmItem : selector.evaluate())
						{
							nodes.add((XdmNode)xdmItem);
						}
					}
					catch (final SaxonApiException e)
					{
						throw new PipelineException(e, "error while compiling @select: %s", declaredPort.getSelect());
					}
				}
				else
				{
					nodes.add(node);
				}
			}
		}

		return nodes;
	}


	public void writeNodes(final XdmNode... nodes)
	{
		for (final XdmNode node : nodes)
		{
			portBindings.add(new AbstractBoundPortBinding()
			{
				public Iterable<XdmNode> readNodes()
				{
					return Collections.singletonList(node);
				}
			});
		}
	}


	public void pipe(final EnvironmentPort port)
	{
		LOG.entry();
		assert port != null : getDeclaredPort();
		assert port != this : getDeclaredPort();
		LOG.trace("{} -> {}", port.getDeclaredPort(), getDeclaredPort());

		portBindings.add(new AbstractBoundPortBinding()
		{
			public Iterable<XdmNode> readNodes()
			{
				LOG.entry();
				LOG.trace("from {}", port);
				return port.readNodes();
			}
		});
	}


	@Override
	public String toString()
	{
		return String.format("%s[%s/%s]", getClass().getSimpleName(), declaredPort.getStepName(), declaredPort
			.getPortName());
	}
}
