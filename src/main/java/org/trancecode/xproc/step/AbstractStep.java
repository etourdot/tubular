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

import org.trancecode.core.CollectionUtil;
import org.trancecode.xml.Location;
import org.trancecode.xml.SaxonUtil;
import org.trancecode.xproc.AbstractHasLocation;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.EnvironmentPort;
import org.trancecode.xproc.Option;
import org.trancecode.xproc.Parameter;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcNamespaces;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.Port.Type;

import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.Processor;
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
	public static final QName ELEMENT_PARAMETER = XProcNamespaces.XPROC.newSaxonQName("parameter");
	public static final QName ATTRIBUTE_PARAMETER_NAME = new QName("name");
	public static final QName ATTRIBUTE_PARAMETER_VALUE = new QName("value");

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

	protected XLogger log = XLoggerFactory.getXLogger(getClass());

	protected final Map<QName, Parameter> parameters = CollectionUtil.newSmallWriteOnceMap();
	protected final Map<QName, Variable> variables = CollectionUtil.newSmallWriteOnceMap();

	protected final Map<String, Port> ports = CollectionUtil.newSmallWriteOnceMap();

	protected final String name;


	protected AbstractStep(final String name, final Location location)
	{
		super(location);

		assert name != null : getClass().getName();
		this.name = name;
	}


	public void declareOption(final Option option)
	{
		assert !variables.containsKey(name);
		variables.put(option.getName(), option);
	}


	public void declareVariable(final Variable variable)
	{
		assert !variables.containsKey(name);
		variables.put(variable.getName(), variable);
	}


	public String getName()
	{
		return name;
	}


	public final Port declareInputPort(
		final String portName, final Location location, final boolean primary, final boolean sequence)
	{
		return declarePort(portName, location, Type.INPUT, primary, sequence);
	}


	public final Port declareParameterPort(
		final String portName, final Location location, final boolean primary, final boolean sequence)
	{
		return declarePort(portName, location, Type.PARAMETER, primary, sequence);
	}


	public final Port declareOutputPort(
		final String portName, final Location location, final boolean primary, final boolean sequence)
	{
		return declarePort(portName, location, Type.OUTPUT, primary, sequence);
	}


	private final Port declarePort(
		final String portName, final Location location, final Type type, final boolean primary, final boolean sequence)
	{
		// FIXME what of port that are explictly set to "not primary"?
		final Port port = new Port(getName(), portName, location, type, primary, sequence);
		addPort(port);
		return port;
	}


	public final Port declarePort(final Port port)
	{
		if (port.isInput())
		{
			return declareInputPort(port.getPortName(), port.getLocation(), port.isPrimary(), port.isSequence());
		}
		else if (port.isOutput())
		{
			return declareOutputPort(port.getPortName(), port.getLocation(), port.isPrimary(), port.isSequence());
		}
		else
		{
			assert port.isParameter();
			return declareParameterPort(port.getPortName(), port.getLocation(), port.isPrimary(), port.isSequence());
		}
	}


	public final Option declareOption(
		final QName optionName, final String select, final boolean required, final Location location)
	{
		final Option option = new Option(optionName, select, required, location);
		declareOption(option);
		return option;
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


	protected EnvironmentPort getEnvironmentPort(final String name, final Environment environment)
	{
		log.entry(name);

		final Port declaredPort = ports.get(name);
		assert declaredPort != null : "name = " + name + " ; ports = " + ports;

		if (!environment.getPorts().containsKey(declaredPort.getPortReference()))
		{
			final EnvironmentPort environmentPort = environment.addEnvironmentPort(declaredPort);
			if (declaredPort.isInput() && isPrimary(declaredPort))
			{
				if (declaredPort.getPortBindings().isEmpty())
				{ // Set a pipe binding to default readable port if no binding on primary input port
					final EnvironmentPort defaultPort = environment.getDefaultReadablePort();
					if (defaultPort != null)
					{
						environmentPort.pipe(defaultPort);
					}
				}

				log.trace("bindings = {}", declaredPort.getPortBindings());
				environment.setDefaultReadablePort(environmentPort);
			}

			if (declaredPort.getPortName().equals(XProcPorts.XPATH_CONTEXT))
			{
				if (declaredPort.getPortBindings().isEmpty())
				{
					final EnvironmentPort xpathContextPort = environment.getXPathContextPort();
					if (xpathContextPort != null)
					{
						environmentPort.pipe(xpathContextPort);
					}
				}
			}

			if (isXPathContextPort(declaredPort))
			{
				environment.setXPathContextPort(environmentPort);
			}

			assert !(!declaredPort.getPortBindings().isEmpty() && declaredPort.isOutput()) : "port is already bound: "
				+ declaredPort;

			return environmentPort;
		}

		final EnvironmentPort environmentPort = environment.getEnvironmentPort(declaredPort);
		assert environmentPort != null : "name = " + name + " ; environment.ports = " + environment.getPorts();

		return environmentPort;
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


	protected EnvironmentPort getInputEnvironmentPort(final String name, final Environment environment)
	{
		final EnvironmentPort port = getEnvironmentPort(name, environment);
		assert port.getDeclaredPort().isInput();
		return port;
	}


	protected EnvironmentPort getOutputEnvironmentPort(final String name, final Environment environment)
	{
		final EnvironmentPort port = getEnvironmentPort(name, environment);
		assert port.getDeclaredPort().isOutput();
		return port;
	}


	protected Iterable<XdmNode> readNodes(final String name, final Environment environment)
	{
		log.entry(name);
		final Iterable<XdmNode> nodes = getInputEnvironmentPort(name, environment).readNodes();
		log.trace("nodes = {}", SaxonUtil.nodesToString(nodes));
		return nodes;
	}


	protected XdmNode readNode(final String name, final Environment environment)
	{
		return readNode(getInputEnvironmentPort(name, environment));
	}


	private void setupInputEnvironmentPorts(final Environment environment)
	{
		log.entry();

		for (final Port port : Iterables.concat(getInputPorts(), getParameterPorts()))
		{
			getEnvironmentPort(port.getPortName(), environment);
		}
	}


	protected void bindOutputEnvironmentPorts(final Environment environment)
	{
		bindOutputEnvironmentPorts(environment, environment);
	}


	protected void bindOutputEnvironmentPorts(final Environment sourceEnvironment, final Environment resultEnvironment)
	{
		log.entry();

		for (final Port port : getOutputPorts())
		{
			log.trace("port = {}", port);
			final EnvironmentPort environmentPort = getEnvironmentPort(port.getPortName(), resultEnvironment);

			log.trace("primary port = {}", isPrimary(port));
			if (isPrimary(port))
			{
				log.trace("port bindings = {}", port.getPortBindings());
				// Set a pipe binding to default readable port if no binding on primary port
				if (port.getPortBindings().isEmpty() && sourceEnvironment != resultEnvironment)
				{
					final EnvironmentPort defaultPort = sourceEnvironment.getDefaultReadablePort();
					if (defaultPort != null && defaultPort != environmentPort)
					{
						environmentPort.pipe(defaultPort);
					}
				}

				resultEnvironment.setDefaultReadablePort(environmentPort);
			}
		}
	}


	protected Environment newResultEnvironment(final Environment environment)
	{
		// create result environment and import from given environment
		final Environment resultEnvironment = environment.newFollowingStepEnvironment();

		setupInputEnvironmentPorts(resultEnvironment);
		setLocalVariables(resultEnvironment, Iterables.concat(variables.values(), parameters.values()));

		return resultEnvironment;
	}


	protected abstract void doRun(final Environment environment) throws Exception;


	public Environment run(final Environment environment)
	{
		log.entry(getType(), getName());
		log.trace("variables = {}", environment.getVariables());
		log.trace("declared variables = {}", variables);

		final Environment resultEnvironment = newResultEnvironment(environment);

		try
		{
			doRun(resultEnvironment);
		}
		catch (final Exception e)
		{
			// TODO handle exception
			throw new IllegalStateException(e);
		}
		bindOutputEnvironmentPorts(resultEnvironment);

		return resultEnvironment;
	}


	protected void setDefaultReadablePort(final Environment environment)
	{
		final Port primaryOutputPort = getPrimaryOutputPort();
		if (primaryOutputPort != null)
		{
			log.trace("new default readable port: {}", primaryOutputPort.getPortName());
			final EnvironmentPort environmentPort = environment.getEnvironmentPort(primaryOutputPort);
			assert environmentPort != null : "step = " + getName() + " ; port = " + primaryOutputPort.toString();
			environment.setDefaultReadablePort(environmentPort);
		}
	}


	protected Port getPrimaryOutputPort()
	{
		for (final Port port : ports.values())
		{
			if (port.isOutput() && isPrimary(port))
			{
				return port;
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


	protected Iterable<Port> getInputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_INPUT_PORT);
	}


	protected Iterable<Port> getOutputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_OUTPUT_PORT);
	}


	protected Iterable<Port> getParameterPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_PARAMETER_PORT);
	}


	public void withOption(final QName name, final String select)
	{
		assert variables.get(name) instanceof Option;
		variables.get(name).setSelect(select);
	}


	public void withParam(final QName name, final String select, final String value, final Location location)
	{
		assert !parameters.containsKey(name);

		final Parameter parameter = new Parameter(name, select, location);
		if (value != null)
		{
			parameter.setValue(value);
		}

		parameters.put(name, parameter);
	}


	public void withOptionValue(final QName name, final String value)
	{
		assert variables.containsKey(name) : "step = " + getName() + " ; option = " + name + " ; variables = "
			+ variables.keySet();
		assert variables.get(name) instanceof Option : variables.get(name).getClass().getName();
		variables.get(name).setValue(value);
	}


	public boolean hasOptionDeclared(final QName name)
	{
		return variables.containsKey(name);
	}


	protected static String getVariable(final QName name, final Environment environment, final String defaultValue)
	{
		final String value = environment.getVariable(name);
		if (value != null)
		{
			return value;
		}

		return defaultValue;
	}


	private void setLocalVariables(final Environment environment, final Iterable<Variable> variables)
	{
		log.entry();

		if (Iterables.isEmpty(variables))
		{
			return;
		}

		for (final Variable variable : variables)
		{
			final String value;
			try
			{
				value = variable.evaluate(environment);
			}
			catch (final Exception e)
			{
				throw new PipelineException(e, "error while evaluating variable $%s in step %s of type %s @ %s",
					getName(), getName(), getType(), getLocation());
			}

			log.trace("{} = {}", variable.getName(), value);

			if (variable instanceof Parameter)
			{
				final EnvironmentPort parametersPort = environment.getDefaultParametersPort();
				assert parametersPort != null;
				final XdmNode parameterNode =
					newParameterElement(variable.getName(), value, environment.getProcessor());
				parametersPort.writeNodes(parameterNode);
			}
			else
			{
				environment.setLocalVariable(variable.getName(), value);
			}
		}
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


	protected void writeNodes(final String portName, final Environment environment, final XdmNode... nodes)
	{
		log.entry(portName, nodes.length, SaxonUtil.nodesToString(nodes));
		getEnvironmentPort(portName, environment).writeNodes(nodes);
	}


	protected static XdmNode newParameterElement(final QName name, final String value, final Processor processor)
	{
		// TODO
		return null;
	}


	protected static XdmNode newResultElement(final String value, final Processor processor)
	{
		// TODO use s9api directly
		final String document =
			String.format("<c:result xmlns:c=\"%s\">%s</c:result>", XProcNamespaces.URI_XPROC_STEP, value);
		try
		{
			return processor.newDocumentBuilder().build(new StreamSource(new StringReader(document)));
		}
		catch (final SaxonApiException e)
		{
			throw new IllegalStateException(e);
		}
	}


	protected Map<QName, String> readParameters(final String portName, final Environment environment)
	{
		final EnvironmentPort port = getEnvironmentPort(portName, environment);

		final Map<QName, String> parameters = CollectionUtil.newSmallWriteOnceMap();
		for (final XdmNode parameterNode : port.readNodes())
		{
			final XPathCompiler xpathCompiler = environment.getProcessor().newXPathCompiler();
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
}
