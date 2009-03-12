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

import org.trancecode.xml.Location;
import org.trancecode.xproc.CompoundStep;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Step;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractCompoundStep extends AbstractStep implements CompoundStep
{
	protected final List<Step> steps = Lists.newArrayList();


	protected AbstractCompoundStep(final String name, final Location location)
	{
		super(name, location);
	}


	public Iterable<Step> getSteps()
	{
		return Iterables.unmodifiableIterable(steps);
	}


	public void addStep(final Step step)
	{
		steps.add(step);
	}


	@Override
	protected void doRun(final Environment environment)
	{
		log.entry();
		log.trace("steps = {}", steps);

		Environment currentEnvironment = environment.newChildStepEnvironment();
		for (final Step step : steps)
		{
			currentEnvironment = step.run(currentEnvironment);
		}

		bindOutputEnvironmentPorts(currentEnvironment, environment);
	}


	protected Environment runSteps(final List<Step> steps, final Environment environment)
	{
		log.entry();
		log.trace("steps = {}", steps);

		Environment currentEnvironment = environment;
		for (final Step step : steps)
		{
			currentEnvironment = step.run(currentEnvironment);
		}

		return currentEnvironment;
	}
}
