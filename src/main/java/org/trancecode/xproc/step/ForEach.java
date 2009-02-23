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
import org.trancecode.xproc.EnvironmentPort;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Port.Type;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.parser.StepFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class ForEach extends AbstractCompoundStep
{
	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new ForEach(name, location);
		}


		public QName getStepType()
		{
			return STEP_FOR_EACH;
		}
	}


	public ForEach(final String name, final Location location)
	{
		super(name, location);

		declareInputPort(PORT_ITERATION_SOURCE, location, true, true);
		declareOutputPort(PORT_RESULT, location, true, true);
	}


	public QName getType()
	{
		return STEP_FOR_EACH;
	}


	private Port newIterationPort(final XdmNode node)
	{
		final Port port = new Port(name, PORT_ITERATION_NODE, getLocation(), Type.INPUT, false, false);
		port.getPortBindings().add(new InlinePortBinding(node, getLocation()));
		return port;
	}


	@Override
	public void doRun(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		for (final XdmNode node : readNodes(PORT_ITERATION_SOURCE, environment))
		{
			log.trace("new iteration: %s", node);
			final Environment iterationEnvironment = environment.newChildStepEnvironment();
			final EnvironmentPort environmentPort = iterationEnvironment.addEnvironmentPort(newIterationPort(node));
			iterationEnvironment.setDefaultReadablePort(environmentPort);

			final Environment resultEnvironment = runSteps(steps, iterationEnvironment);
			getOutputEnvironmentPort(PORT_RESULT, environment).pipe(resultEnvironment.getDefaultReadablePort());
		}
	}
}
