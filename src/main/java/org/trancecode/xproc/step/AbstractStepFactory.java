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
import org.trancecode.xml.Location;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.StepProcessor;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.parser.StepFactory;

import java.util.Collections;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractStepFactory implements StepFactory
{
	protected static final Iterable<Port> EMPTY_PORT_LIST = Collections.emptyList();
	protected static final Iterable<Variable> EMPTY_VARIABLE_LIST = Collections.emptyList();
	protected static final String DEFAULT_STEP_NAME = "null";

	private final QName stepType;
	private final StepProcessor stepProcessor;
	private final boolean isCompoundStep;
	private final Iterable<Port> declaredPorts;
	private final Iterable<Variable> declaredVariables;


	protected AbstractStepFactory(
		final QName stepType, final StepProcessor stepProcessor, final boolean isCompoundStep,
		final Iterable<Port> declaredPorts, final Iterable<Variable> declaredVariables)
	{
		assert stepType != null;
		this.stepType = stepType;

		assert stepProcessor != null;
		this.stepProcessor = stepProcessor;

		this.isCompoundStep = isCompoundStep;

		assert declaredPorts != null;
		this.declaredPorts = declaredPorts;

		assert declaredVariables != null;
		this.declaredVariables = declaredVariables;
	}


	@Override
	public final Step newStep(final String name, final Location location)
	{
		final Step declaredStep = GenericStep.newStep(stepType, name, location, stepProcessor, isCompoundStep);

		final Step stepWithDeclaredPorts =
			CollectionUtil.apply(declaredStep, declaredPorts, new BinaryFunction<Step, Step, Port>()
			{
				@Override
				public Step evaluate(final Step step, final Port port)
				{
					return step.declarePort(port.setStepName(name).setLocation(location));
				}
			});

		final Step stepWithDeclaredVariables =
			CollectionUtil.apply(stepWithDeclaredPorts, declaredVariables, new BinaryFunction<Step, Step, Variable>()
			{
				@Override
				public Step evaluate(final Step step, final Variable variable)
				{
					return step.declareVariable(variable.setLocation(location));
				}
			});

		return stepWithDeclaredVariables;
	}
}
