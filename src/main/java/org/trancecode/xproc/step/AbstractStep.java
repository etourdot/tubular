/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.trancecode.xproc.step;

import org.trancecode.core.CollectionUtil;
import org.trancecode.log.Logger;
import org.trancecode.log.LoggerHelpers;
import org.trancecode.log.LoggerManager;
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
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.Port.Type;

import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractStep extends AbstractHasLocation
	implements Step, XProcSteps, XProcPorts, XProcOptions, XProcNamespaces, LoggerHelpers
{
	public static final QName ELEMENT_PARAMETER = NAMESPACE_XPROC.newSaxonQName("parameter");
	public static final QName ATTRIBUTE_PARAMETER_NAME = new QName("name");
	public static final QName ATTRIBUTE_PARAMETER_VALUE = new QName("value");

	protected Logger log = LoggerManager.getLogger(this);

	protected final Map<QName, Parameter> parameters = CollectionUtil.newSmallWriteOnceMap();
	protected final Map<QName, Variable> variables = CollectionUtil.newSmallWriteOnceMap();

	protected final Map<String, Port> ports = CollectionUtil.newSmallWriteOnceMap();

	protected final String name;


	protected AbstractStep(final String name, final Location location)
	{
		super(location);

		assert name != null : getClass().getName();
		this.name = name;

		log = log.getChildLogger(name);
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


	public final Option declareOption(
		final QName optionName, final String select, final boolean required, final Location location)
	{
		final Option option = new Option(optionName, select, required, location);
		declareOption(option);
		return option;
	}


	public void addPort(final Port port)
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
		log.trace("%s name = %s", METHOD_NAME, name);

		final Port declaredPort = ports.get(name);
		assert declaredPort != null : "name = " + name + " ; ports = " + ports;

		if (!environment.getPorts().containsKey(declaredPort.getPortReference()))
		{
			final EnvironmentPort environmentPort = environment.addEnvironmentPort(declaredPort);
			if (isInput(declaredPort) && isPrimary(declaredPort))
			{
				if (declaredPort.getPortBindings().isEmpty())
				{ // Set a pipe binding to default readable port if no binding on primary input port
					final EnvironmentPort defaultPort = environment.getDefaultReadablePort();
					if (defaultPort != null)
					{
						environmentPort.pipe(defaultPort);
					}
				}

				log.trace("bindings = %s", declaredPort.getPortBindings());
				environment.setDefaultReadablePort(environmentPort);
			}

			if (declaredPort.getPortName().equals(PORT_XPATH_CONTEXT))
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

			assert !(!declaredPort.getPortBindings().isEmpty() && isOutput(declaredPort)) : "port is already bound: "
				+ declaredPort;

			return environmentPort;
		}

		final EnvironmentPort environmentPort = environment.getEnvironmentPort(declaredPort);
		assert environmentPort != null : "name = " + name + " ; environment.ports = " + environment.getPorts();

		return environmentPort;
	}


	private boolean isXPathContextPort(final Port port)
	{
		if (isInput(port))
		{
			if (port.getPortName().equals(PORT_XPATH_CONTEXT))
			{
				return true;
			}

			if (isPrimary(port))
			{
				return !ports.containsKey(PORT_XPATH_CONTEXT);
			}
		}

		return false;
	}


	protected EnvironmentPort getInputEnvironmentPort(final String name, final Environment environment)
	{
		final EnvironmentPort port = getEnvironmentPort(name, environment);
		assert isInput(port.getDeclaredPort());
		return port;
	}


	protected EnvironmentPort getOutputEnvironmentPort(final String name, final Environment environment)
	{
		final EnvironmentPort port = getEnvironmentPort(name, environment);
		assert isOutput(port.getDeclaredPort());
		return port;
	}


	protected Iterable<XdmNode> readNodes(final String name, final Environment environment)
	{
		log.trace("%s name = %s", METHOD_NAME, name);
		final Iterable<XdmNode> nodes = getInputEnvironmentPort(name, environment).readNodes();
		log.trace("nodes = %s", SaxonUtil.nodesToString(nodes));
		return nodes;
	}


	protected XdmNode readNode(final String name, final Environment environment)
	{
		return readNode(getInputEnvironmentPort(name, environment));
	}


	private void setupInputEnvironmentPorts(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		for (final Port port : Iterables.concat(getPorts(Type.INPUT), getPorts(Type.PARAMETER)))
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
		for (final Port port : getPorts(Type.OUTPUT))
		{
			log.trace("%s port = %s", METHOD_NAME, port);
			final EnvironmentPort environmentPort = getEnvironmentPort(port.getPortName(), resultEnvironment);

			log.trace("primary port = %s", isPrimary(port));
			if (isPrimary(port))
			{
				log.trace("port bindings = %s", port.getPortBindings());
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
		setLocalVariables(resultEnvironment, Iterables.concat(variables.values(), parameters.values()), log);

		return resultEnvironment;
	}


	protected abstract void doRun(final Environment environment);


	public Environment run(final Environment environment)
	{
		log.trace("%s stepType = %s ; stepName = %s", METHOD_NAME, getType(), getName());
		log.trace("variables = %s", environment.getVariables());
		log.trace("declared variables = %s", variables);

		final Environment resultEnvironment = newResultEnvironment(environment);

		doRun(resultEnvironment);
		bindOutputEnvironmentPorts(resultEnvironment);

		return resultEnvironment;
	}


	protected void setDefaultReadablePort(final Environment environment)
	{
		final Port primaryOutputPort = getPrimaryOutputPort();
		if (primaryOutputPort != null)
		{
			log.trace("new default readable port: %s", primaryOutputPort.getPortName());
			final EnvironmentPort environmentPort = environment.getEnvironmentPort(primaryOutputPort);
			assert environmentPort != null : "step = " + getName() + " ; port = " + primaryOutputPort.toString();
			environment.setDefaultReadablePort(environmentPort);
		}
	}


	protected Port getPrimaryOutputPort()
	{
		for (final Port port : ports.values())
		{
			if (port.getType() == Type.OUTPUT && isPrimary(port))
			{
				return port;
			}
		}

		return null;
	}


	private static boolean isOutput(final Port port)
	{
		return port.getType().equals(Type.OUTPUT);
	}


	private static boolean isInput(final Port port)
	{
		return port.getType().equals(Type.INPUT);
	}


	private static boolean isParameter(final Port port)
	{
		return port.getType().equals(Type.PARAMETER);
	}


	private boolean isPrimary(final Port port)
	{
		return isPrimary(port, this);
	}


	private static boolean isPrimary(final Port port, final Step step)
	{
		return isPrimary(port, step.getPorts(port.getType()));
	}


	private static boolean isPrimary(final Port port, final Collection<Port> ports)
	{
		assert port != null;

		if (port.isPrimary())
		{
			return true;
		}

		if (ports.size() == 1)
		{
			return true;
		}

		return false;
	}


	public Collection<Port> getPorts(final Type type)
	{
		final Collection<Port> selectedPorts = new LinkedHashSet<Port>(ports.size());
		for (final Port port : ports.values())
		{
			if (port.getType() == type)
			{
				selectedPorts.add(port);
			}
		}

		return selectedPorts;
	}


	public void withOption(final QName name, final String select)
	{
		assert variables.get(name) instanceof Option;
		variables.get(name).setSelect(select);
	}


	public void withParam(final Parameter parameter)
	{
		assert !parameters.containsKey(parameter.getName());

		parameters.put(parameter.getName(), parameter);
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


	private void setLocalVariables(final Environment environment, final Iterable<Variable> variables, final Logger log)
	{
		log.trace("%s", METHOD_NAME);

		if (Iterables.isEmpty(variables))
		{
			return;
		}

		for (final Variable variable : variables)
		{
			final String value;
			if (variable.getValue() != null)
			{
				value = variable.getValue();
			}
			else
			{
				final String select = variable.getSelect();
				log.trace(
					"%s step = %s ; variable = %s ; select = %s", METHOD_NAME, getName(), variable.getName(), select);

				if (select == null)
				{
					if (variable.isRequired())
					{
						throw new PipelineException("Unbound variable %s in step %s of type %s @ %s", variable
							.getName(), getName(), getType(), getLocation());
					}

					value = null;
				}
				else
				{
					try
					{
						value = evaluateXPath(select, environment).toString();
					}
					catch (final Exception e)
					{
						throw new PipelineException(e, "error while evaluating variable $%s ; select = %s", getName(),
							select);
					}
				}
			}

			log.trace("%s = %s", variable.getName(), value);

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


	protected XdmValue evaluateXPath(final String select, final Environment environment)
	{
		assert select != null;
		assert environment != null;
		log.trace("%s select = %s", METHOD_NAME, select);

		try
		{
			final XPathCompiler xpathCompiler = environment.getProcessor().newXPathCompiler();
			final String pipelineSystemId = environment.getPipeline().getLocation().getSystemId();
			if (pipelineSystemId != null)
			{
				xpathCompiler.setBaseURI(URI.create(pipelineSystemId));
			}
			for (final Map.Entry<QName, String> variableEntry : environment.getVariables().entrySet())
			{
				if (variableEntry.getValue() != null)
				{
					xpathCompiler.declareVariable(variableEntry.getKey());
				}
			}

			final XPathSelector selector = xpathCompiler.compile(select).load();
			final XdmNode xpathContextNode = environment.getXPathContextNode();
			if (xpathContextNode != null)
			{
				log.trace("xpathContextNode = %s", xpathContextNode);
				selector.setContextItem(xpathContextNode);
			}

			for (final Map.Entry<QName, String> variableEntry : environment.getVariables().entrySet())
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
		log.trace("%s portName = %s ; %s nodes = %s", METHOD_NAME, portName, nodes.length, SaxonUtil
			.nodesToString(nodes));
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
			String.format("<c:result xmlns:c=\"%s\">%s</c:result>", NAMESPACE_XPROC_STEP_URI, value);
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
