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
import org.trancecode.xproc.Step;
import org.trancecode.xproc.StepProcessor;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class IdentityStepProcessor implements StepProcessor
{
	public static final IdentityStepProcessor INSTANCE = new IdentityStepProcessor();

	private static final Logger LOG = LoggerFactory.getLogger(IdentityStepProcessor.class);


	@Override
	public Environment run(final Step step, final Environment environment)
	{
		LOG.trace("step = {}", step.getName());
		assert step.getType().equals(XProcSteps.IDENTITY);

		final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);

		final Environment resultEnvironment =
			stepEnvironment.writeNodes(step.getName(), XProcPorts.RESULT, stepEnvironment.readNodes(
				step.getName(), XProcPorts.SOURCE));

		return resultEnvironment.setupOutputPorts(step);
	}
}
