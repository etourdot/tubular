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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
	protected final List<EnvironmentPortBinding> portBindings;
	private final XPathExecutable select;


	public static EnvironmentPort newEnvironmentPort(final Port declaredPort, final Environment environment)
	{
		assert declaredPort != null;
		assert environment != null;

		final List<EnvironmentPortBinding> portBindings =
			ImmutableList.copyOf(Iterables.transform(
				declaredPort.getPortBindings(), new Function<PortBinding, EnvironmentPortBinding>()
				{
					@Override
					public EnvironmentPortBinding apply(final PortBinding portBinding)
					{
						return portBinding.newEnvironmentPortBinding(environment);
					}
				}));

		final String declaredPortSelect = declaredPort.getSelect();
		final XPathExecutable select;
		if (declaredPortSelect != null)
		{
			try
			{
				select = environment.getConfiguration().getProcessor().newXPathCompiler().compile(declaredPortSelect);
			}
			catch (final SaxonApiException e)
			{
				throw XProcExceptions.xd0023(declaredPort.getLocation(), declaredPortSelect, e.getMessage());
			}
		}
		else
		{
			select = null;
		}

		return new EnvironmentPort(declaredPort, portBindings, select);
	}


	private EnvironmentPort(
		final Port declaredPort, final Iterable<EnvironmentPortBinding> portBindings, final XPathExecutable select)
	{
		this.declaredPort = declaredPort;
		this.portBindings = Lists.newArrayList(portBindings);
		this.select = select;
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
						throw XProcExceptions.xd0023(declaredPort.getLocation(), declaredPort.getSelect(), e
							.getMessage());
					}
				}
				else
				{
					nodes.add(node);
				}
			}
		}

		// defensive programming
		return ImmutableList.copyOf(nodes);
	}


	public void writeNodes(final XdmNode... nodes)
	{
		final List<XdmNode> nodeList = ImmutableList.of(nodes);
		portBindings.add(new AbstractBoundPortBinding()
		{
			public Iterable<XdmNode> readNodes()
			{
				return nodeList;
			}
		});
	}


	public void pipe(final EnvironmentPort port)
	{
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
