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
package org.trancecode.xproc.step;

import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.core.CollectionUtil;
import org.trancecode.xml.Location;
import org.trancecode.xml.SaxonUtil;
import org.trancecode.xproc.AbstractHasLocation;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPort;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcPorts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractStep extends AbstractHasLocation implements Step
{
	private static final Predicate<Port> PREDICATE_IS_INPUT_PORT = new Predicate<Port>()
	{
		@Override
		public boolean apply(final Port port)
		{
			return port.isInput();
		}
	};

	private static final Predicate<Port> PREDICATE_IS_OUTPUT_PORT = new Predicate<Port>()
	{
		@Override
		public boolean apply(final Port port)
		{
			return port.isOutput();
		}
	};

	private static final Predicate<Port> PREDICATE_IS_PARAMETER_PORT = new Predicate<Port>()
	{
		@Override
		public boolean apply(final Port port)
		{
			return port.isParameter();
		}
	};

	private final Predicate<Port> PREDICATE_IS_XPATH_CONTEXT_PORT = new Predicate<Port>()
	{
		@Override
		public boolean apply(final Port port)
		{
			return isXPathContextPort(port);
		}
	};

	protected XLogger log = XLoggerFactory.getXLogger(getClass());

	protected final Map<QName, Variable> parameters = CollectionUtil.newSmallWriteOnceMap();
	protected final Map<QName, Variable> variables = CollectionUtil.newSmallWriteOnceMap();

	protected final Map<String, Port> ports = CollectionUtil.newSmallWriteOnceMap();

	protected final String name;


	protected AbstractStep(final String name, final Location location)
	{
		super(location);

		assert name != null : getClass().getName();
		this.name = name;
	}


	public Step declareVariable(final Variable variable)
	{
		assert !variables.containsKey(name);
		variables.put(variable.getName(), variable);
		return this;
	}


	public String getName()
	{
		return name;
	}


	public final Step declarePort(final Port port)
	{
		addPort(port);
		return this;
	}


	protected void addPort(final Port port)
	{
		ports.put(port.getPortName(), port);
	}


	public Port getPort(final String name)
	{
		assert ports.containsKey(name) : "step = " + getName() + " ; port = " + name + " ; ports = " + ports.keySet();
		return ports.get(name);
	}


	public Map<String, Port> getPorts()
	{
		return Collections.unmodifiableMap(ports);
	}


	private boolean isXPathContextPort(final Port port)
	{
		if (port.isInput())
		{
			if (port.getPortName().equals(XProcPorts.XPATH_CONTEXT))
			{
				return true;
			}

			if (isPrimary(port))
			{
				return !ports.containsKey(XProcPorts.XPATH_CONTEXT);
			}
		}

		return false;
	}


	protected Iterable<XdmNode> readNodes(final String portName, final Environment environment)
	{
		log.trace("portName = {}", portName);
		final Iterable<XdmNode> nodes = environment.getEnvironmentPort(name, portName).readNodes();
		log.trace("nodes = {}", SaxonUtil.nodesToString(nodes));
		return nodes;
	}


	protected XdmNode readNode(final String portName, final Environment environment)
	{
		return readNode(environment.getEnvironmentPort(name, portName));
	}


	protected abstract Environment doRun(final Environment environment) throws Exception;


	public Environment run(final Environment environment)
	{
		log.entry(getType(), getName());
		log.trace("declared variables = {}", variables);

		final Environment resultEnvironment;
		try
		{
			resultEnvironment = doRun(environment.newFollowingStepEnvironment(this));
		}
		catch (final XProcException e)
		{
			throw (XProcException)e.fillInStackTrace();
		}
		catch (final Exception e)
		{
			// TODO handle exception
			throw new IllegalStateException(e);
		}

		// TODO check result environment

		return resultEnvironment.setupOutputPorts(this);
	}


	@ReturnsNullable
	public Port getPrimaryInputPort()
	{
		final List<Port> inputPorts = ImmutableList.copyOf(getInputPorts());
		log.trace("inputPorts = {}", inputPorts);
		if (inputPorts.size() == 1)
		{
			final Port inputPort = Iterables.getOnlyElement(inputPorts);
			if (!inputPort.isNotPrimary())
			{
				return inputPort;
			}
		}

		for (final Port inputPort : inputPorts)
		{
			if (inputPort.isPrimary())
			{
				return inputPort;
			}
		}

		return null;
	}


	@ReturnsNullable
	public Port getPrimaryParameterPort()
	{
		final List<Port> parameterPorts = ImmutableList.copyOf(getParameterPorts());
		log.trace("parameterPorts = {}", parameterPorts);
		if (parameterPorts.size() == 1)
		{
			final Port parameterPort = Iterables.getOnlyElement(parameterPorts);
			if (!parameterPort.isNotPrimary())
			{
				return parameterPort;
			}
		}

		for (final Port parameterPort : parameterPorts)
		{
			if (parameterPort.isPrimary())
			{
				return parameterPort;
			}
		}

		return null;
	}


	@ReturnsNullable
	public Port getPrimaryOutputPort()
	{
		final List<Port> outputPorts = ImmutableList.copyOf(getOutputPorts());
		log.trace("outputPorts = {}", outputPorts);
		if (outputPorts.size() == 1)
		{
			final Port outputPort = Iterables.getOnlyElement(outputPorts);
			if (!outputPort.isNotPrimary())
			{
				return outputPort;
			}
		}

		for (final Port outputPort : outputPorts)
		{
			if (outputPort.isPrimary())
			{
				return outputPort;
			}
		}

		return null;
	}


	private boolean isPrimary(final Port port)
	{
		if (port.isParameter())
		{
			return isPrimary(port, getParameterPorts());
		}

		if (port.isInput())
		{
			return isPrimary(port, getInputPorts());
		}

		assert port.isOutput();
		return isPrimary(port, getOutputPorts());
	}


	private static boolean isPrimary(final Port port, final Iterable<Port> ports)
	{
		assert port != null;

		if (port.isNotPrimary())
		{
			return false;
		}

		if (port.isPrimary())
		{
			return true;
		}

		if (Iterables.size(ports) == 1)
		{
			return true;
		}

		return false;
	}


	public Iterable<Port> getInputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_INPUT_PORT);
	}


	public Iterable<Port> getOutputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_OUTPUT_PORT);
	}


	public Iterable<Port> getParameterPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_PARAMETER_PORT);
	}


	public Step withOption(final QName name, final String select)
	{
		assert variables.containsKey(name);
		assert variables.get(name).isOption();
		variables.put(name, variables.get(name).setSelect(select));
		return this;
	}


	public Step withParam(final QName name, final String select, final String value, final Location location)
	{
		assert !parameters.containsKey(name);
		parameters.put(name, Variable.newParameter(name, location).setSelect(select).setValue(value));
		return this;
	}


	public Step withOptionValue(final QName name, final String value)
	{
		assert variables.containsKey(name) : "step = " + getName() + " ; option = " + name + " ; variables = "
			+ variables.keySet();
		assert variables.get(name).isOption();
		variables.put(name, variables.get(name).setValue(value));
		return this;
	}


	public boolean hasOptionDeclared(final QName name)
	{
		return variables.containsKey(name);
	}


	protected static XdmNode readNode(final EnvironmentPort port)
	{
		try
		{
			return Iterables.getOnlyElement(port.readNodes());
		}
		catch (final NoSuchElementException e)
		{
			throw new PipelineException("no node could be read from %s", port);
		}
	}


	protected Map<QName, String> readParameters(final String portName, final Environment environment)
	{
		final Map<QName, String> parameters = CollectionUtil.newSmallWriteOnceMap();
		for (final XdmNode parameterNode : readNodes(portName, environment))
		{
			final XPathCompiler xpathCompiler = environment.getConfiguration().getProcessor().newXPathCompiler();
			try
			{
				final XPathSelector nameSelector = xpathCompiler.compile("string(//@name)").load();
				nameSelector.setContextItem(parameterNode);
				final String name = nameSelector.evaluateSingle().toString();
				final XPathSelector valueSelector = xpathCompiler.compile("string(//@value)").load();
				valueSelector.setContextItem(parameterNode);
				final String value = valueSelector.evaluateSingle().toString();
				// TODO name should be real QName
				parameters.put(new QName(name), value);
			}
			catch (final SaxonApiException e)
			{
				throw new PipelineException(e);
			}
		}

		return parameters;
	}


	@Override
	public String toString()
	{
		return String.format(
			"%s name = %s ; ports = %s ; variables = %s", getClass().getSimpleName(), name, ports, variables);
	}


	public Step setPortBindings(final String portName, final PortBinding... portBindings)
	{
		withPort(getPort(portName).setPortBindings(portBindings));
		return this;
	}


	public Step setPortBindings(final String portName, final Iterable<PortBinding> portBindings)
	{
		withPort(getPort(portName).setPortBindings(portBindings));
		return this;
	}


	public Step withPort(final Port port)
	{
		assert ports.containsKey(port.getPortName());
		ports.put(port.getPortName(), port);
		return this;
	}


	@ReturnsNullable
	public Port getXPathContextPort()
	{
		return Iterables.getOnlyElement(Iterables.filter(getInputPorts(), PREDICATE_IS_XPATH_CONTEXT_PORT), null);
	}


	public Iterable<Variable> getVariables()
	{
		return variables.values();
	}


	public Step declarePorts(final Iterable<Port> ports)
	{
		for (final Port port : ports)
		{
			declarePort(port);
		}

		return this;
	}


	@Override
	public boolean isCompoundStep()
	{
		return false;
	}


	@Override
	public Step setLocation(final Location location)
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public Step setName(final String name)
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public Step declareVariables(final Iterable<Variable> variables)
	{
		throw new UnsupportedOperationException();
	}
}
