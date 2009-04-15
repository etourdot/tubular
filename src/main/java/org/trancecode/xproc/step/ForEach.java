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
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.parser.StepFactory;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class ForEach extends AbstractCompoundStep
{
	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new ForEach(name, location);
		}
	};


	private ForEach(final String name, final Location location)
	{
		super(name, location);

		addPort(Port.newInputPort(name, XProcPorts.ITERATION_SOURCE, location).setSequence(true));
		addPort(Port.newOutputPort(name, XProcPorts.RESULT, location).setSequence(true));
	}


	public QName getType()
	{
		return XProcSteps.FOR_EACH;
	}


	private Port newIterationPort(final XdmNode node)
	{
		return Port.newInputPort(name, XProcPorts.ITERATION_NODE, getLocation()).setPrimary(false).setSequence(false)
			.setPortBindings(new InlinePortBinding(node, getLocation()));
	}


	@Override
	public Environment doRun(final Environment environment)
	{
		log.entry();

		final List<XdmNode> nodes = Lists.newArrayList();
		for (final XdmNode node : readNodes(XProcPorts.ITERATION_SOURCE, environment))
		{
			log.trace("new iteration: {}", node);
			final Environment iterationEnvironment = environment.newChildStepEnvironment();
			final EnvironmentPort environmentPort = iterationEnvironment.addEnvironmentPort(newIterationPort(node));
			iterationEnvironment.setDefaultReadablePort(environmentPort);

			final Environment resultEnvironment = runSteps(steps, iterationEnvironment);
			Iterables.addAll(nodes, resultEnvironment.getDefaultReadablePort().readNodes());
		}

		return environment.writeNodes(getName(), XProcPorts.RESULT, nodes);
	}
}
