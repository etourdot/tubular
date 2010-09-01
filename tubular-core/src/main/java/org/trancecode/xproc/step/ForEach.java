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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;

/**
 * @author Herve Quiroz
 */
public class ForEach extends AbstractCompoundStepProcessor
{
    public static final ForEach INSTANCE = new ForEach();

    private static final Logger LOG = Logger.getLogger(ForEach.class);

    private static final Iterable<Port> PORTS = ImmutableList.of(Port.newInputPort(XProcPorts.ITERATION_SOURCE)
            .setSequence(true), Port.newOutputPort(XProcPorts.RESULT).setSequence(true));
    public static final Step STEP = Step.newStep(XProcSteps.FOR_EACH, INSTANCE, true).declarePorts(PORTS);

    private ForEach()
    {
        // single instance
    }

    private Port newIterationPort(final Step step, final XdmNode node)
    {
        return Port.newInputPort(step.getName(), XProcPorts.CURRENT, step.getLocation()).setPrimary(false).setSequence(
                false).setPortBindings(new InlinePortBinding(node, step.getLocation()));
    }

    @Override
    public Environment run(final Step step, final Environment environment)
    {
        LOG.trace("step = {}", step.getName());
        assert step.getType().equals(XProcSteps.FOR_EACH);

        final List<XdmNode> nodes = Lists.newArrayList();

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);

        for (final XdmNode node : stepEnvironment.readNodes(step.getPortReference(XProcPorts.ITERATION_SOURCE)))
        {
            LOG.trace("new iteration: {}", node);
            final Port iterationPort = newIterationPort(step, node);
            final Environment iterationEnvironment = environment.newChildStepEnvironment().addPorts(
                    EnvironmentPort.newEnvironmentPort(iterationPort, environment)).setDefaultReadablePort(
                    step.getPortReference(XProcPorts.CURRENT));

            final Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
            Iterables.addAll(nodes, resultEnvironment.getDefaultReadablePort().readNodes());
        }

        final Environment resultEnvironment = stepEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT),
                nodes);

        return resultEnvironment.setupOutputPorts(step);
    }
}
