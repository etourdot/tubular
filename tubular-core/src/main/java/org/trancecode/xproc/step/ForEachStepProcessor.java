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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.concurrent.ParallelIterables;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;

/**
 * @author Herve Quiroz
 */
public final class ForEachStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ForEachStepProcessor.class);

    @Override
    public Step stepDeclaration()
    {
        final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.ITERATION_SOURCE).setSequence(true),
                Port.newOutputPort(XProcPorts.RESULT).setSequence(true));
        return Step.newStep(XProcSteps.FOR_EACH, this, true).declarePorts(ports);
    }

    @Override
    public QName stepType()
    {
        return XProcSteps.FOR_EACH;
    }

    private Port newIterationPort(final Step step, final XdmNode node)
    {
        return Port.newInputPort(step.getName(), XProcPorts.CURRENT, step.getLocation()).setPrimary(false)
                .setSequence(false).setPortBindings(new InlinePortBinding(node, step.getLocation()));
    }

    @Override
    public Environment run(final Step step, final Environment environment)
    {
        LOG.trace("step = {}", step.getName());
        assert step.getType().equals(XProcSteps.FOR_EACH);

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);

        final Function<XdmNode, Iterable<XdmNode>> loopFunction = new Function<XdmNode, Iterable<XdmNode>>()
        {
            @Override
            public Iterable<XdmNode> apply(final XdmNode node)
            {
                LOG.trace("new iteration: {}", node);
                final Port iterationPort = newIterationPort(step, node);
                final Environment iterationEnvironment = environment.newChildStepEnvironment()
                        .addPorts(EnvironmentPort.newEnvironmentPort(iterationPort, environment))
                        .setDefaultReadablePort(step.getPortReference(XProcPorts.CURRENT));

                final Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
                return resultEnvironment.getDefaultReadablePort().readNodes();
            }
        };

        final Iterable<XdmNode> inputNodes = stepEnvironment.readNodes(step
                .getPortReference(XProcPorts.ITERATION_SOURCE));
        final Iterable<XdmNode> resultNodes = ImmutableList.copyOf(Iterables.concat(ParallelIterables.transform(
                inputNodes, loopFunction, environment.getPipelineContext().getExecutorService())));

        return stepEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT), resultNodes).setupOutputPorts(step);
    }
}
