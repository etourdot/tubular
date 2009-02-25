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
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.parser.StepFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Identity extends AbstractStep
{
	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new Identity(name, null, location);
		}
	}


	private Identity(final String name, final String libraryName, final Location location)
	{
		super(name, location);

		declareInputPort(PORT_SOURCE, location, false, true);
		declareOutputPort(PORT_RESULT, location, false, true);
	}


	public QName getType()
	{
		return XProcSteps.STEP_IDENTITY;
	}


	@Override
	protected void doRun(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		for (final XdmNode node : readNodes(PORT_SOURCE, environment))
		{
			writeNodes(PORT_RESULT, environment, node);
		}
	}
}
