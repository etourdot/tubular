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

import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.trancecode.concurrent.TcFutures;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.port.EnvironmentPort;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractCompoundStepProcessor implements StepProcessor
{
    private static final Logger LOG = Logger.getLogger(AbstractCompoundStepProcessor.class);

    @Override
    public Environment run(final Step step, final Environment environment)
    {
        LOG.trace("{@method} step = {} ; type = {}", step.getName(), step.getType());
        assert step.isCompoundStep();

        environment.setCurrentEnvironment();

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
        Environment resultEnvironment = runSteps(step.getSubpipeline(), stepEnvironment);
        resultEnvironment = stepEnvironment.setupOutputPorts(step, resultEnvironment);
        Steps.writeLogs(step, resultEnvironment);
        return resultEnvironment;
    }

    protected Environment runSteps(final Iterable<Step> steps, final Environment environment)
    {
        LOG.trace("steps = {}", steps);

        final Environment initialEnvironment = environment.newChildStepEnvironment();
        final EnvironmentPort parametersPort = environment.getDefaultParametersPort();
        LOG.trace("  parametersPort = {}", parametersPort);

        final Map<Step, Iterable<Step>> stepDependencies = Step.getSubpipelineStepDependencies(steps);
        final Map<Step, Future<Environment>> stepResults = new ConcurrentHashMap<>();
        final List<Future<Environment>> results = Lists.newArrayList();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        for (final Step step : steps)
        {
            final Future<Environment> result = environment.getPipelineContext().getExecutor()
                    .submit(() -> {
                        // shortcut in case an error was reported by another
                        // task
                        if (error.get() != null)
                        {
                            throw new IllegalStateException(error.get());
                        }

                        Environment inputEnvironment = initialEnvironment;
                        for (final Step dependency : stepDependencies.get(step))
                        {
                            try
                            {
                                final Environment dependencyResult = stepResults.get(dependency).get();
                                inputEnvironment = inputEnvironment.addPorts(dependencyResult.getOutputPorts());
                                inputEnvironment = inputEnvironment.setDefaultReadablePort(dependencyResult
                                        .getDefaultReadablePort());
                                inputEnvironment = inputEnvironment.setDefaultParametersPort(parametersPort);
                                inputEnvironment = inputEnvironment.setXPathContextPort(dependencyResult
                                        .getXPathContextPort());
                            }
                            catch (final ExecutionException e)
                            {
                                throw Throwables.propagate(e.getCause());
                            }
                        }

                        Environment.setCurrentNamespaceContext(step.getNode());
                        inputEnvironment.setCurrentEnvironment();
                        return step.run(inputEnvironment);
                    });
            stepResults.put(step, result);
            results.add(result);
        }

        final Iterable<Environment> resultEnvironments;
        try
        {
            resultEnvironments = TcFutures.get(results);
        }
        catch (final ExecutionException e)
        {
            TcFutures.cancel(results);
            throw Throwables.propagate(e.getCause());
        }
        catch (final InterruptedException e)
        {
            throw new IllegalStateException(e);
        }

        Environment resultEnvironment = Iterables.getLast(resultEnvironments, initialEnvironment);
        for (final Environment intermediateResultEnvironment : Iterables.filter(resultEnvironments,
                Predicates.notNull()))
        {
            for (final EnvironmentPort port : intermediateResultEnvironment.getOutputPorts())
            {
                if (!resultEnvironment.getPorts().containsKey(port.getPortReference()))
                {
                    resultEnvironment = resultEnvironment.addPorts(port);
                }
            }
        }

        return resultEnvironment;
    }
}
