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

import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.xproc.step.Pipeline;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Environment
{
	private static final XLogger LOG = XLoggerFactory.getXLogger(Environment.class);

	private EnvironmentPort defaultReadablePort;
	private final Map<QName, String> variables = Maps.newLinkedHashMap();
	private final Map<QName, String> inheritedVariables = Maps.newLinkedHashMap();
	private final Map<QName, String> localVariables = Maps.newLinkedHashMap();
	private final Configuration configuration;
	private final Map<PortReference, EnvironmentPort> ports = Maps.newLinkedHashMap();
	private final Pipeline pipeline;
	private final Processor processor;
	private EnvironmentPort defaultParametersPort;
	private EnvironmentPort xpathContextPort;


	public static Environment newEnvironment(final Pipeline pipeline, final Configuration configuration)
	{
		return new Environment(pipeline, configuration);
	}


	private Environment(final Pipeline pipeline, final Configuration configuration)
	{
		assert pipeline != null;
		this.pipeline = pipeline;

		assert configuration != null;
		this.configuration = configuration;

		processor = configuration.getProcessor();
	}


	private Environment(final Environment environment)
	{
		assert environment != null;

		pipeline = environment.getPipeline();
		configuration = environment.getConfiguration();
		processor = environment.getProcessor();

		inheritedVariables.putAll(environment.getVariables());
		variables.putAll(environment.getVariables());

		ports.putAll(environment.getPorts());
		if (environment.getDefaultReadablePort() != null)
		{
			setDefaultReadablePort(environment.getDefaultReadablePort());
		}

		if (environment.getXPathContextPort() != null)
		{
			setXPathContextPort(environment.getXPathContextPort());
		}

		assert invariant();
	}


	public boolean invariant()
	{
		final Set<QName> variableNames = Sets.newHashSet();
		variableNames.addAll(localVariables.keySet());
		variableNames.addAll(inheritedVariables.keySet());
		return variableNames.equals(variables.keySet());
	}


	public Environment newFollowingStepEnvironment()
	{
		final Environment environment = new Environment(this);
		assert environment.invariant();
		return environment;
	}


	public Environment newChildStepEnvironment()
	{
		final Environment environment = new Environment(this);
		environment.inheritedVariables.putAll(localVariables);
		assert environment.invariant();

		return environment;
	}


	public void setLocalVariable(final QName name, final String value)
	{
		localVariables.put(name, value);
		variables.put(name, value);
		assert invariant();
	}


	public Map<QName, String> getLocalVariables()
	{
		// XXX slow
		return Collections.unmodifiableMap(localVariables);
	}


	public EnvironmentPort getDefaultReadablePort()
	{
		return defaultReadablePort;
	}


	public EnvironmentPort getXPathContextPort()
	{
		return xpathContextPort;
	}


	public void setXPathContextPort(final EnvironmentPort xpathContextPort)
	{
		LOG.entry(xpathContextPort);
		this.xpathContextPort = xpathContextPort;
	}


	public Configuration getConfiguration()
	{
		return configuration;
	}


	public Map<QName, String> getVariables()
	{
		// XXX slow
		return Collections.unmodifiableMap(variables);
	}


	public String getVariable(final QName name)
	{
		return variables.get(name);
	}


	public void setDefaultReadablePort(final EnvironmentPort port)
	{
		assert port != null;
		assert ports.containsValue(port);
		LOG.entry(port);

		defaultReadablePort = port;
	}


	public Map<PortReference, EnvironmentPort> getPorts()
	{
		return Collections.unmodifiableMap(ports);
	}


	public EnvironmentPort getEnvironmentPort(final Port port)
	{
		return getEnvironmentPort(port.getPortReference());
	}


	public EnvironmentPort getEnvironmentPort(final PortReference portReference)
	{
		assert ports.containsKey(portReference) : "port = " + portReference + " ; ports = " + ports;
		return ports.get(portReference);
	}


	public EnvironmentPort addEnvironmentPort(final Port port)
	{
		LOG.entry(port);
		assert port.getPortReference().equals(port.getPortReference());
		assert !ports.containsKey(port.getPortReference());
		final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port, this);
		ports.put(port.getPortReference(), environmentPort);
		return environmentPort;
	}


	public Pipeline getPipeline()
	{
		return pipeline;
	}


	public URI getBaseUri()
	{
		return URI.create(pipeline.getLocation().getSystemId());
	}


	public Processor getProcessor()
	{
		return processor;
	}


	public EnvironmentPort getDefaultParametersPort()
	{
		return this.defaultParametersPort;
	}


	public void setDefaultParametersPort(final EnvironmentPort defaultParametersPort)
	{
		this.defaultParametersPort = defaultParametersPort;
	}


	@ReturnsNullable
	public XdmNode getXPathContextNode()
	{
		// TODO cache

		final EnvironmentPort xpathContextPort = getXPathContextPort();
		if (xpathContextPort != null)
		{
			final Iterator<XdmNode> contextNodes = xpathContextPort.readNodes().iterator();
			if (!contextNodes.hasNext())
			{
				return null;
			}

			final XdmNode contextNode = contextNodes.next();
			if (xpathContextPort.getDeclaredPort().getPortName().equals(XProcPorts.XPATH_CONTEXT))
			{
				// TODO XProc error
				assert !contextNodes.hasNext();
			}

			return contextNode;
		}

		return null;
	}


	public XdmValue evaluateXPath(final String select)
	{
		assert select != null;
		LOG.entry(select);

		try
		{
			final XPathCompiler xpathCompiler = getProcessor().newXPathCompiler();
			final String pipelineSystemId = getPipeline().getLocation().getSystemId();
			if (pipelineSystemId != null)
			{
				xpathCompiler.setBaseURI(URI.create(pipelineSystemId));
			}
			for (final Map.Entry<QName, String> variableEntry : getVariables().entrySet())
			{
				if (variableEntry.getValue() != null)
				{
					xpathCompiler.declareVariable(variableEntry.getKey());
				}
			}

			final XPathSelector selector = xpathCompiler.compile(select).load();
			final XdmNode xpathContextNode = getXPathContextNode();
			if (xpathContextNode != null)
			{
				LOG.trace("xpathContextNode = {}", xpathContextNode);
				selector.setContextItem(xpathContextNode);
			}

			for (final Map.Entry<QName, String> variableEntry : getVariables().entrySet())
			{
				if (variableEntry.getValue() != null)
				{
					selector.setVariable(variableEntry.getKey(), new XdmAtomicValue(variableEntry.getValue()));
				}
			}

			return selector.evaluate();
		}
		catch (final Exception e)
		{
			throw new PipelineException(e, "error while evaluating XPath query: %s", select);
		}
	}
}
