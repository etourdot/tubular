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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.concurrent.TcFutures;
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

        final List<XdmNode> inputNodes = ImmutableList.copyOf(stepEnvironment.readNodes(step
                .getPortReference(XProcPorts.ITERATION_SOURCE)));
        final List<Callable<Iterable<XdmNode>>> tasks = Lists.newArrayListWithCapacity(inputNodes.size());
        for (int i = 0; i < inputNodes.size(); i++)
        {
            final XdmNode inputNode = inputNodes.get(i);
            tasks.add(new Callable<Iterable<XdmNode>>()
            {
                @Override
                public Iterable<XdmNode> call()
                {
                    LOG.trace("new iteration: {}", inputNode);
                    final Port iterationPort = newIterationPort(step, inputNode);
                    final Environment iterationEnvironment = environment.newChildStepEnvironment()
                            .addPorts(EnvironmentPort.newEnvironmentPort(iterationPort, environment))
                            .setDefaultReadablePort(step.getPortReference(XProcPorts.CURRENT));
                    final Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
                    return resultEnvironment.getDefaultReadablePort().readNodes();
                }
            });
        }
        final Iterable<Future<Iterable<XdmNode>>> futureResultNodes = TcFutures.submit(environment.getPipelineContext()
                .getExecutorService(), tasks);
        final Iterable<XdmNode> resultNodes = Iterables.concat(TcFutures.get(futureResultNodes));

        return stepEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT), resultNodes).setupOutputPorts(step);
    }
}
