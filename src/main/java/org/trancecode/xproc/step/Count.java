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
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.parser.StepFactory;

import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Count extends AbstractStep
{
	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new Count(name, location);
		}
	};


	private Count(final String name, final Location location)
	{
		super(name, location);

		declareInputPort(XProcPorts.SOURCE, location, false, true);
		declareOutputPort(XProcPorts.RESULT, location, false, false);

		declareOption(XProcOptions.LIMIT, "0", false, location);
	}


	@Override
	protected void doRun(final Environment environment)
	{
		// TODO improve performance with "limit" option
		final int count = Iterables.size(readNodes(XProcPorts.SOURCE, environment));
		log.trace("count = {}", count);
		final int limit = Integer.parseInt(environment.getVariable(XProcOptions.LIMIT));
		log.trace("limit = {}", limit);
		final int result = (limit > 0 ? Math.min(count, limit) : count);
		log.trace("result = {}", result);

		writeNodes(XProcPorts.RESULT, environment, newResultElement(Integer.toString(result), environment
			.getConfiguration().getProcessor()));
	}


	@Override
	public QName getType()
	{
		return XProcSteps.COUNT;
	}
}
