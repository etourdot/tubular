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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmValue;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.Saxon;
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
    public Step getStepDeclaration()
    {
        final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT).setSequence(false)
                .setPrimary(false));
        return Step.newStep(XProcSteps.CHOOSE, this, true).declarePorts(ports);
    }

    @Override
    public QName getStepType()
    {
        return XProcSteps.CHOOSE;
    }

    @Override
    public Environment run(final Step chooseStep, final Environment environment)
    {
        LOG.trace("chooseStep = {}", chooseStep.getName());
        assert chooseStep.getType().equals(XProcSteps.CHOOSE);
        assert chooseStep.isCompoundStep();

        final Environment stepEnvironment = environment.newFollowingStepEnvironment(chooseStep);
        for (final Step whenStep : chooseStep.getSubpipeline())
        {
            assert XProcSteps.WHEN_STEPS.contains(whenStep.getType());
            final Step normalizedWhenStep = PipelineStepProcessor.addImplicitPorts(whenStep);
            Environment resultEnvironment = runSteps(ImmutableList.of(normalizedWhenStep), stepEnvironment);

            if (resultEnvironment != null)
            {
                final List<EnvironmentPort> newPorts = Lists.newArrayList();

                for (final Port port : normalizedWhenStep.getOutputPorts())
                {
                    final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(
                            port.setStepName(chooseStep.getName()), stepEnvironment);
                    newPorts.add(environmentPort.pipe(resultEnvironment.getEnvironmentPort(port)));
                }

                resultEnvironment = resultEnvironment.addPorts(newPorts);
                final Port primaryOutputPort = normalizedWhenStep.getPrimaryOutputPort();
                if (primaryOutputPort != null)
                {
                    resultEnvironment = resultEnvironment.setDefaultReadablePort(chooseStep
                            .getPortReference(primaryOutputPort.getPortName()));
                }

                return resultEnvironment;
            }
        }

        throw XProcExceptions.xd0004(chooseStep.getLocation());
    }

    public static final class WhenStepProcessor extends AbstractWhenStepProcessor
    {
        @Override
        public Step getStepDeclaration()
        {
            final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT)
                    .setSequence(false).setPrimary(false));
            return Step.newStep(XProcSteps.WHEN, this, true).declarePorts(ports)
                    .declareVariable(Variable.newOption(XProcOptions.TEST).setRequired(true));
        }

        @Override
        public QName getStepType()
        {
            return XProcSteps.WHEN;
        }

        @Override
        protected boolean doTest(final Step whenStep, final Environment environment)
        {
            final Environment resultEnvironment = environment.newChildStepEnvironment();
            final String test = whenStep.getVariable(XProcOptions.TEST).getValue();
            LOG.trace("test = {}", test);
            final XdmValue result = resultEnvironment.evaluateXPath(test);
            LOG.trace("result = {}", result);
            return Saxon.isTrue(result);
        }
    }

    public static final class OtherwiseStepProcessor extends AbstractWhenStepProcessor
    {
        @Override
        public Step getStepDeclaration()
        {
            final Iterable<Port> ports = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT)
                    .setSequence(false).setPrimary(false));
            return Step.newStep(XProcSteps.OTHERWISE, this, true).declarePorts(ports);
        }

        @Override
        public QName getStepType()
        {
            return XProcSteps.OTHERWISE;
        }

        @Override
        protected boolean doTest(final Step otherwiseStep, final Environment environment)
        {
            return true;
        }
    }

    private abstract static class AbstractWhenStepProcessor extends AbstractCompoundStepProcessor implements
            CoreStepProcessor
    {
        @Override
        public Environment run(final Step whenStep, final Environment environment)
        {
            LOG.trace("step = {}", whenStep.getName());

            final Environment stepEnvironment = environment.newFollowingStepEnvironment(whenStep);
            if (!test(whenStep, stepEnvironment))
            {
                return null;
            }

            final Environment resultEnvironment = runSteps(whenStep.getSubpipeline(), stepEnvironment);

            return stepEnvironment.setupOutputPorts(whenStep, resultEnvironment);
        }

        protected boolean test(final Step whenStep, final Environment environment)
        {
            LOG.trace("name = {} ; type = {}", whenStep.getName(), whenStep.getType());
            final boolean result = doTest(whenStep, environment);
            LOG.trace("result = {}", result);
            return result;
        }

        protected abstract boolean doTest(final Step whenStep, final Environment environment);
    }
}
