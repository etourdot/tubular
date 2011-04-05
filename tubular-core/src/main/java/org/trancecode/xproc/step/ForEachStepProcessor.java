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

import com.google.common.base.Function;
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
                .setSequence(true), Port.newInputPort(XProcPorts.CURRENT));
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
        final List<Callable<Environment>> tasks = Lists.newArrayListWithCapacity(inputNodes.size());
        for (int i = 0; i < inputNodes.size(); i++)
        {
            final int iterationPosition = i + 1;
            final XdmNode inputNode = inputNodes.get(i);
            tasks.add(new Callable<Environment>()
            {
                @Override
                public Environment call()
                {
                    LOG.trace("iteration {}/{}: {}", iterationPosition, iterationSize, inputNode);

                    final int oldIterationPosition = IterationPositionXPathExtensionFunction
                            .setIterationPosition(iterationPosition);
                    final int oldIterationSize = IterationSizeXPathExtensionFunction.setIterationSize(iterationSize);
                    try
                    {
                        final EnvironmentPort iterationPort = EnvironmentPort.newEnvironmentPort(
                                newIterationPort(step, inputNode), environment);
                        Environment iterationEnvironment = environment.newChildStepEnvironment();
                        iterationEnvironment = iterationEnvironment.addPorts(iterationPort);
                        iterationEnvironment = iterationEnvironment.setDefaultReadablePort(iterationPort);
                        iterationEnvironment = iterationEnvironment.setXPathContextPort(iterationPort);
                        iterationEnvironment = iterationEnvironment.setupVariables(step);
                        iterationEnvironment.setCurrentEnvironment();
                        Environment resultEnvironment = runSteps(step.getSubpipeline(), iterationEnvironment);
                        resultEnvironment = stepEnvironment.setupOutputPorts(step, resultEnvironment);
                        Steps.writeLogs(step, resultEnvironment);
                        return resultEnvironment;
                    }
                    finally
                    {
                        IterationPositionXPathExtensionFunction.setIterationPosition(oldIterationPosition);
                        IterationSizeXPathExtensionFunction.setIterationSize(oldIterationSize);
                    }
                }
            });
        }

        LOG.trace("  {}: submitting {size} iteration tasks...", step, tasks);
        final Iterable<Future<Environment>> futureResultEnvironments = TcFutures.submit(environment
                .getPipelineContext().getExecutor(), tasks);
        final Iterable<Environment> iterationResultEnvironments;
        try
        {
            iterationResultEnvironments = ImmutableList.copyOf(TcFutures.get(futureResultEnvironments, true));
        }
        catch (final ExecutionException e)
        {
            throw Throwables.propagate(e.getCause());
        }
        catch (final InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
        LOG.trace("  {}: done executing iteration tasks", step);

        LOG.trace("  {}: merging output ports from iteration tasks...", step);
        Environment resultEnvironment = stepEnvironment;
        for (final Port outputPort : step.getOutputPorts())
        {
            LOG.trace("    port = {}", outputPort);
            resultEnvironment = resultEnvironment.addPorts(EnvironmentPort.newEnvironmentPort(
                    Port.newOutputPort(step.getName(), outputPort.getPortName(), outputPort.getLocation()),
                    stepEnvironment));

            final Iterable<XdmNode> resultNodes = ImmutableList.copyOf(Iterables.concat(Iterables.transform(
                    iterationResultEnvironments, new Function<Environment, Iterable<XdmNode>>()
                    {
                        @Override
                        public Iterable<XdmNode> apply(final Environment environment)
                        {
                            return environment.readNodes(outputPort.getPortReference());
                        }
                    })));
            LOG.trace("     resultNodes = {size}", resultNodes);
            resultEnvironment = resultEnvironment.writeNodes(outputPort.getPortReference(), resultNodes);
        }

        resultEnvironment = resultEnvironment.setPrimaryOutputPortAsDefaultReadablePort(step, stepEnvironment);
        resultEnvironment = resultEnvironment.setDefaultReadablePortAsXPathContextPort();

        return resultEnvironment;
    }
}
