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

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.trancecode.collection.TcIterables;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.Variable;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Herve Quiroz
 */
public final class ChooseStepProcessor extends AbstractCompoundStepProcessor implements CoreStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ChooseStepProcessor.class);

    @Override
    public Step stepDeclaration()
    {
        final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT).setSequence(false)
                .setPrimary(false));
        return Step.newStep(XProcSteps.CHOOSE, this, true).declarePorts(ports);
    }

    @Override
    public QName stepType()
    {
        return XProcSteps.CHOOSE;
    }

    @Override
    public Environment run(final Step step, final Environment environment)
    {
        LOG.trace("step = {}", step.getName());
        assert step.getType().equals(XProcSteps.CHOOSE);
        assert step.isCompoundStep();

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
        for (final Step whenStep : step.getSubpipeline())
        {
            assert XProcSteps.WHEN_STEPS.contains(whenStep.getType());
            Environment resultEnvironment = runSteps(ImmutableList.of(whenStep), stepEnvironment);

            if (resultEnvironment != null)
            {
                final List<EnvironmentPort> newPorts = Lists.newArrayList();

                for (final Port port : whenStep.getOutputPorts())
                {
                    final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(
                            port.setStepName(step.getName()), stepEnvironment);
                    newPorts.add(environmentPort.pipe(resultEnvironment.getEnvironmentPort(port)));
                }

                resultEnvironment = resultEnvironment.addPorts(newPorts);
                final Port primaryOutputPort = whenStep.getPrimaryOutputPort();
                if (primaryOutputPort != null)
                {
                    resultEnvironment = resultEnvironment.setDefaultReadablePort(step
                            .getPortReference(primaryOutputPort.getPortName()));
                }

                return resultEnvironment;
            }
        }

        throw XProcExceptions.xd0004(step.getLocation());
    }

    public static final class WhenStepProcessor extends AbstractWhenStepProcessor
    {
        @Override
        public Step stepDeclaration()
        {
            final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT)
                    .setSequence(false).setPrimary(false));
            return Step.newStep(XProcSteps.WHEN, this, true).declarePorts(ports)
                    .declareVariable(Variable.newOption(XProcOptions.TEST).setRequired(true));
        }

        @Override
        public QName stepType()
        {
            return XProcSteps.WHEN;
        }

        @Override
        protected boolean doTest(final Step step, final Environment environment)
        {
            final Environment resultEnvironment = environment.newChildStepEnvironment();
            final String test = step.getVariable(XProcOptions.TEST).getValue();
            LOG.trace("test = {}", test);
            final XdmValue result = resultEnvironment.evaluateXPath(test);
            LOG.trace("result = {}", result);

            if (result.size() == 0)
            {
                return false;
            }

            if (result.size() > 1)
            {
                return true;
            }

            final XdmItem resultNode = result.iterator().next();
            if (resultNode.isAtomicValue())
            {
                try
                {
                    return ((XdmAtomicValue) resultNode).getBooleanValue();
                }
                catch (final SaxonApiException e)
                {
                    // TODO error handling
                    throw new IllegalStateException(e);
                }
            }

            return true;
        }
    }

    public static final class OtherwiseStepProcessor extends AbstractWhenStepProcessor
    {
        @Override
        public Step stepDeclaration()
        {
            final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT)
                    .setSequence(false).setPrimary(false));
            return Step.newStep(XProcSteps.OTHERWISE, this, true).declarePorts(ports);
        }

        @Override
        public QName stepType()
        {
            return XProcSteps.OTHERWISE;
        }

        @Override
        protected boolean doTest(final Step step, final Environment environment)
        {
            return true;
        }
    }

    private static abstract class AbstractWhenStepProcessor extends AbstractCompoundStepProcessor implements
            CoreStepProcessor
    {
        @Override
        public Environment run(final Step step, final Environment environment)
        {
            LOG.trace("step = {}", step.getName());

            final Environment stepEnvironment = environment.newFollowingStepEnvironment(step);
            if (!test(step, stepEnvironment))
            {
                return null;
            }

            Environment resultEnvironment = runSteps(step.getSubpipeline(), stepEnvironment);

            if (Iterables.isEmpty(step.getOutputPorts()))
            {
                final Step lastStep = TcIterables.getLast(step.getSubpipeline());

                final Port primaryOutputPort = lastStep.getPrimaryOutputPort();
                if (primaryOutputPort != null)
                {
                    final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(primaryOutputPort
                            .setStepName(step.getName()).setPortBindings(), stepEnvironment);
                    resultEnvironment = resultEnvironment.addPorts(environmentPort.pipe(resultEnvironment
                            .getEnvironmentPort(primaryOutputPort)));
                }

                return resultEnvironment;
            }

            return stepEnvironment.setupOutputPorts(step, resultEnvironment);
        }

        protected boolean test(final Step step, final Environment environment)
        {
            LOG.trace("name = {} ; type = {}", step.getName(), step.getType());
            final boolean result = doTest(step, environment);
            LOG.trace("result = {}", result);
            return result;
        }

        protected abstract boolean doTest(final Step step, final Environment environment);
    }
}
