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
import org.trancecode.xproc.AbstractHasLocation;
import org.trancecode.xproc.CompoundStep;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.StepProcessor;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcPorts;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.sf.saxon.s9api.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class GenericStep extends AbstractHasLocation implements Step, CompoundStep
{
	private static final Logger LOG = LoggerFactory.getLogger(GenericStep.class);
	private static final Map<QName, Variable> EMPTY_VARIABLE_MAP = Collections.emptyMap();
	private static final Map<String, Port> EMPTY_PORT_MAP = Collections.emptyMap();
	private static final List<Step> EMPTY_STEP_LIST = Collections.emptyList();

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

	private final Map<QName, Variable> parameters;
	private final Map<QName, Variable> variables;

	private final Map<String, Port> ports;

	private final QName type;
	private final String name;
	private final StepProcessor stepProcessor;
	private final List<Step> steps;
	private final boolean compoundStep;


	public static Step newStep(
		final QName type, final String name, final Location location, final StepProcessor stepProcessor,
		final boolean compoundStep)
	{
		return new GenericStep(type, name, location, stepProcessor, compoundStep, EMPTY_VARIABLE_MAP,
			EMPTY_VARIABLE_MAP, EMPTY_PORT_MAP, EMPTY_STEP_LIST);
	}


	private GenericStep(
		final QName type, final String name, final Location location, final StepProcessor stepProcessor,
		final boolean compoundStep, final Map<QName, Variable> variables, final Map<QName, Variable> parameters,
		final Map<String, Port> ports, final Iterable<Step> steps)
	{
		super(location);

		assert type != null;
		this.type = type;

		assert name != null;
		this.name = name;

		assert stepProcessor != null;
		this.stepProcessor = stepProcessor;

		this.compoundStep = compoundStep;

		this.variables = ImmutableMap.copyOf(variables);
		this.parameters = ImmutableMap.copyOf(parameters);
		this.ports = ImmutableMap.copyOf(ports);
		this.steps = ImmutableList.copyOf(steps);
	}


	@Override
	public boolean isCompoundStep()
	{
		return compoundStep;
	}


	@Override
	public Step declareVariable(final Variable variable)
	{
		assert !variables.containsKey(variable.getName());
		return new GenericStep(type, name, location, stepProcessor, compoundStep, CollectionUtil.copyAndPut(
			variables, variable.getName(), variable), parameters, ports, steps);
	}


	@Override
	public String getName()
	{
		return name;
	}


	@Override
	public final Step declarePort(final Port port)
	{
		LOG.trace("port = {}", port);

		return declarePorts(Collections.singleton(port));
	}


	@Override
	public final Step declarePorts(final Iterable<Port> ports)
	{
		LOG.trace("ports = {}", ports);

		final Map<String, Port> newPorts = Maps.newHashMap(this.ports);
		newPorts.putAll(Maps.uniqueIndex(ports, Port.GET_PORT_NAME_FUNCTION));

		return new GenericStep(type, name, location, stepProcessor, compoundStep, variables, parameters, newPorts,
			steps);
	}


	@Override
	public Port getPort(final String name)
	{
		assert ports.containsKey(name) : "step = " + getName() + " ; port = " + name + " ; ports = " + ports.keySet();
		return ports.get(name);
	}


	@Override
	public Map<String, Port> getPorts()
	{
		return ports;
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


	@Override
	public Environment run(final Environment environment)
	{
		LOG.trace("name = {} ; type = {}", name, type);
		return stepProcessor.run(this, environment);
	}


	@Override
	@ReturnsNullable
	public Port getPrimaryInputPort()
	{
		final List<Port> inputPorts = ImmutableList.copyOf(getInputPorts());
		LOG.trace("inputPorts = {}", inputPorts);
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


	@Override
	@ReturnsNullable
	public Port getPrimaryParameterPort()
	{
		final List<Port> parameterPorts = ImmutableList.copyOf(getParameterPorts());
		LOG.trace("parameterPorts = {}", parameterPorts);
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


	@Override
	@ReturnsNullable
	public Port getPrimaryOutputPort()
	{
		final List<Port> outputPorts = ImmutableList.copyOf(getOutputPorts());
		LOG.trace("outputPorts = {}", outputPorts);
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


	@Override
	public Iterable<Port> getInputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_INPUT_PORT);
	}


	@Override
	public Iterable<Port> getOutputPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_OUTPUT_PORT);
	}


	@Override
	public Iterable<Port> getParameterPorts()
	{
		return Iterables.filter(ports.values(), PREDICATE_IS_PARAMETER_PORT);
	}


	@Override
	public Step withOption(final QName name, final String select)
	{
		assert variables.containsKey(name);
		final Variable variable = variables.get(name);
		assert variable != null;
		assert variable.isOption();

		return new GenericStep(type, this.name, location, stepProcessor, compoundStep, CollectionUtil.copyAndPut(
			variables, variable.getName(), variable.setSelect(select)), parameters, ports, steps);
	}


	@Override
	public Step withParam(final QName name, final String select, final String value, final Location location)
	{
		assert !parameters.containsKey(name);

		return new GenericStep(type, this.name, location, stepProcessor, compoundStep, variables, CollectionUtil
			.copyAndPut(parameters, name, Variable.newParameter(name, location).setSelect(select).setValue(value)),
			ports, steps);
	}


	@Override
	public Step withOptionValue(final QName name, final String value)
	{
		assert variables.containsKey(name);
		final Variable variable = variables.get(name);
		assert variable != null;
		assert variable.isOption();

		return new GenericStep(type, this.name, location, stepProcessor, compoundStep, CollectionUtil.copyAndPut(
			variables, variable.getName(), variable.setValue(value)), parameters, ports, steps);
	}


	@Override
	public boolean hasOptionDeclared(final QName name)
	{
		return variables.containsKey(name);
	}


	@Override
	public String toString()
	{
		return String.format("%s ; name = %s ; ports = %s ; variables = %s", type, name, ports, variables);
	}


	@Override
	public Step setPortBindings(final String portName, final PortBinding... portBindings)
	{
		return withPort(getPort(portName).setPortBindings(portBindings));
	}


	@Override
	public Step withPort(final Port port)
	{
		assert ports.containsKey(port.getPortName());

		return new GenericStep(type, name, location, stepProcessor, compoundStep, variables, parameters, CollectionUtil
			.copyAndPut(ports, port.getPortName(), port), steps);
	}


	@Override
	@ReturnsNullable
	public Port getXPathContextPort()
	{
		return Iterables.getOnlyElement(Iterables.filter(getInputPorts(), PREDICATE_IS_XPATH_CONTEXT_PORT), null);
	}


	@Override
	public Iterable<Variable> getVariables()
	{
		return variables.values();
	}


	@Override
	public QName getType()
	{
		return type;
	}


	@Override
	public Step addStep(final Step step)
	{
		return addSteps(Collections.singleton(step));
	}


	@Override
	public Step addSteps(final Iterable<Step> step)
	{
		return new GenericStep(type, name, location, stepProcessor, compoundStep, variables, parameters, ports,
			Iterables.concat(this.steps, steps));
	}


	@Override
	public Iterable<Step> getSteps()
	{
		return steps;
	}
}
