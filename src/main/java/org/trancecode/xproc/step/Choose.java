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
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.parser.StepFactory;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Choose extends AbstractCompoundStep
{
	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new Choose(name, location);
		}
	};


	private Choose(final String name, final Location location)
	{
		super(name, location);

		addPort(Port.newInputPort(name, XProcPorts.SOURCE, location).setPrimary(true).setSequence(true));
		addPort(Port.newInputPort(name, XProcPorts.XPATH_CONTEXT, location));
		addPort(Port.newOutputPort(name, XProcPorts.RESULT, location));
	}


	@Override
	public Step addStep(final Step step)
	{
		if (!(step instanceof When))
		{
			throw new IllegalArgumentException(step.getClass().getName());
		}

		return super.addStep(step);
	}


	@Override
	protected Environment doRun(final Environment environment)
	{
		for (final Step step : steps)
		{
			final When when = (When)step;
			if (when.test(environment))
			{
				return environment.setupOutputPorts(this, when.run(environment));
			}
		}

		throw XProcExceptions.xd0004(getLocation());
	}


	@Override
	public QName getType()
	{
		return XProcSteps.CHOOSE;
	}
}
