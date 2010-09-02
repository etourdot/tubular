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
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;

/**
 * @author Herve Quiroz
 */
public final class ChooseStepProcessor extends AbstractCompoundStepProcessor
{
    public static final ChooseStepProcessor INSTANCE = new ChooseStepProcessor();

    private static final Iterable<Port> PORTS = ImmutableList.of(Port.newInputPort(XProcPorts.XPATH_CONTEXT)
            .setSequence(false).setPrimary(false));
    public static final Step STEP = Step.newStep(XProcSteps.CHOOSE, INSTANCE, true).declarePorts(PORTS);

    private static final Logger LOG = Logger.getLogger(ChooseStepProcessor.class);

    private ChooseStepProcessor()
    {
        // single instance
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
            Environment resultEnvironment = runSteps(Collections.singleton(whenStep), stepEnvironment);

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
}
