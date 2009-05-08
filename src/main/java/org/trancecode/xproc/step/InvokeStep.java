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

import org.trancecode.core.BinaryFunction;
import org.trancecode.core.CollectionUtil;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.PortReference;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.StepProcessor;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.binding.PipePortBinding;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class InvokeStep implements StepProcessor
{
	public static final InvokeStep INSTANCE = new InvokeStep();

	private static final Logger LOG = LoggerFactory.getLogger(InvokeStep.class);


	private InvokeStep()
	{
		// single instance
	}


	public static Step newInvokeStep(final Step invokedStep)
	{
		assert invokedStep != null;

		Step invokeStep = Step.newStep(invokedStep.getType(), INSTANCE, true).setSteps(ImmutableList.of(invokedStep));

		// declare ports from invoked step
		invokeStep =
			CollectionUtil.apply(invokeStep, invokedStep.getPorts().values(), new BinaryFunction<Step, Step, Port>()
			{
				@Override
				public Step evaluate(final Step step, final Port port)
				{
					if (port.isOutput())
					{
						return step.declarePort(port.pipe(port));
					}

					return step.declarePort(port);
				}
			});

		// declare variables from invoked step
		invokeStep =
			CollectionUtil.apply(invokeStep, invokedStep.getVariables(), new BinaryFunction<Step, Step, Variable>()
			{
				@Override
				public Step evaluate(final Step step, final Variable variable)
				{
					return step.declareVariable(variable);
				}
			});

		return invokeStep;
	}


	private Step setupInvokeStep(final Step invokeStep)
	{
		final Step invokedStep = Iterables.getOnlyElement(invokeStep.getSteps());

		return CollectionUtil.apply(
			invokedStep, invokedStep.getPorts().values(), new BinaryFunction<Step, Step, Port>()
			{
				@Override
				public Step evaluate(final Step step, final Port port)
				{
					if (port.isInput())
					{
						final PortReference localPortReference =
							new PortReference(invokedStep.getName(), port.getPortName());
						LOG.trace("{} -> {}", localPortReference, port.getPortReference());
						final Iterable<PortBinding> portBindings =
							Collections.singleton((PortBinding)new PipePortBinding(localPortReference, invokeStep
								.getLocation()));
						return step.setPortBindings(port.getPortName(), portBindings);
					}
					else
					{
						return step;
					}
				}
			});
	}


	private Step getInvokedStep(final Step invokeStep)
	{
		assert invokeStep != null;
		return Iterables.getOnlyElement(invokeStep.getSteps());
	}


	@Override
	public Environment run(final Step step, final Environment environment)
	{
		LOG.trace("step = {}", step.getName());
		assert step.isCompoundStep();

		final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);

		final Step invokedStep = setupInvokeStep(getInvokedStep(step));
		final Environment invokedStepEnvironment = stepEnvironment.newChildStepEnvironment();
		final Environment resultEnvironment = invokedStep.run(invokedStepEnvironment);

		return stepEnvironment.setupOutputPorts(step, resultEnvironment);
	}
}
