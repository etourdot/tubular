/*
 * Copyright (C) 2008 Herve Quiroz
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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
import org.trancecode.xproc.xpath.IterationPositionXPathExtensionFunction;
import org.trancecode.xproc.xpath.IterationSizeXPathExtensionFunction;

/**
 * @author Herve Quiroz
 */
public final class ForEachStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ForEachStepProcessor.class);

    @Override
    public Step getStepDeclaration()
    {
        final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.ITERATION_SOURCE).setPrimary(true)
                .setSequence(true), Port.newInputPort(XProcPorts.CURRENT), Port.newOutputPort(XProcPorts.RESULT)
                .setSequence(true));
        return Step.newStep(XProcSteps.FOR_EACH, this, true).declarePorts(ports);
    }

    @Override
    public QName getStepType()
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

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(step, false);

        final List<XdmNode> inputNodes = ImmutableList.copyOf(stepEnvironment.readNodes(step
                .getPortReference(XProcPorts.ITERATION_SOURCE)));
        final int iterationSize = inputNodes.size();
        final List<Callable<Iterable<XdmNode>>> tasks = Lists.newArrayListWithCapacity(inputNodes.size());
        for (int i = 0; i < inputNodes.size(); i++)
        {
            final int iterationPosition = i + 1;
            final XdmNode inputNode = inputNodes.get(i);
            tasks.add(new Callable<Iterable<XdmNode>>()
            {
                @Override
                public Iterable<XdmNode> call()
                {
                    LOG.trace("iteration {}/{}: {}", iterationPosition, iterationSize, inputNode);

                    final int oldIterationPosition = IterationPositionXPathExtensionFunction
                            .setIterationPosition(iterationPosition);
                    final int oldIterationSize = IterationSizeXPathExtensionFunction.setIterationSize(iterationSize);
                    try
                    {
                        final Port iterationPort = newIterationPort(step, inputNode);
                        final Environment iterationEnvironment = environment.newChildStepEnvironment()
                                .addPorts(EnvironmentPort.newEnvironmentPort(iterationPort, environment))
                                .setDefaultReadablePort(step.getPortReference(XProcPorts.CURRENT)).setupVariables(step);
                        final Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
                        return resultEnvironment.getDefaultReadablePort().readNodes();
                    }
                    finally
                    {
                        IterationPositionXPathExtensionFunction.setIterationPosition(oldIterationPosition);
                        IterationSizeXPathExtensionFunction.setIterationSize(oldIterationSize);
                    }
                }
            });
        }
        final Iterable<Future<Iterable<XdmNode>>> futureResultNodes = TcFutures.submit(environment.getPipelineContext()
                .getExecutor(), tasks);
        final Iterable<XdmNode> resultNodes;
        try
        {
            resultNodes = Iterables.concat(TcFutures.get(futureResultNodes, true));
        }
        catch (final ExecutionException e)
        {
            throw Throwables.propagate(e.getCause());
        }
        catch (final InterruptedException e)
        {
            throw new IllegalStateException(e);
        }

        return stepEnvironment.writeNodes(step.getPortReference(XProcPorts.RESULT), resultNodes).setupOutputPorts(step);
    }
}
