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

import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.XProcUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class CountStepProcessor extends AbstractStepProcessor
{
	private static final Iterable<Port> PORTS =
		ImmutableList.of(Port.newInputPort(XProcPorts.SOURCE).setSequence(true), Port.newOutputPort(XProcPorts.RESULT));

	private static final Iterable<Variable> VARIABLES =
		ImmutableList.of(Variable.newOption(XProcOptions.LIMIT).setSelect("0").setRequired(false));

	public static final CountStepProcessor INSTANCE = new CountStepProcessor();
	public static final Step STEP =
		GenericStep.newStep(XProcSteps.COUNT, INSTANCE, false).declarePorts(PORTS).declareVariables(VARIABLES);

	private static final Logger LOG = LoggerFactory.getLogger(CountStepProcessor.class);


	@Override
	protected Environment doRun(final Step step, final Environment environment)
	{
		// TODO improve performance with "limit" option
		final int count = Iterables.size(environment.readNodes(step.getName(), XProcPorts.SOURCE));
		LOG.trace("count = {}", count);
		final int limit = Integer.parseInt(environment.getVariable(XProcOptions.LIMIT));
		LOG.trace("limit = {}", limit);
		final int result = (limit > 0 ? Math.min(count, limit) : count);
		LOG.trace("result = {}", result);

		return environment.writeNodes(step.getName(), XProcPorts.RESULT, XProcUtil.newResultElement(Integer
			.toString(result), environment.getConfiguration().getProcessor()));
	}
}
