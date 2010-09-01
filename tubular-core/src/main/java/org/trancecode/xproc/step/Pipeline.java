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

import com.google.common.collect.Iterables;
import net.sf.saxon.s9api.QName;
import org.trancecode.annotation.Nullable;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.StepProcessor;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;

/**
 * @author Herve Quiroz
 */
public class Pipeline extends AbstractCompoundStepProcessor
{
    public static final StepProcessor INSTANCE = new Pipeline();

    public static Step newPipeline(final QName type)
    {
        return Step.newStep(type, INSTANCE, true);
    }

    public static Step addImplicitPorts(final Step pipeline)
    {
        assert Pipeline.isPipeline(pipeline);

        return addImplicitInputPort(addImplicitOutputPort(pipeline));
    }

    private static Step addImplicitInputPort(final Step pipeline)
    {
        if (Iterables.isEmpty(pipeline.getInputPorts()))
        {
            return pipeline.declarePort(Port
                    .newInputPort(pipeline.getName(), XProcPorts.SOURCE, pipeline.getLocation()));
        }

        return pipeline;
    }

    private static Step addImplicitOutputPort(final Step pipeline)
    {
        if (Iterables.isEmpty(pipeline.getOutputPorts()))
        {
            return pipeline.declarePort(Port.newOutputPort(pipeline.getName(), XProcPorts.RESULT, pipeline
                    .getLocation()));
        }

        return pipeline;
    }

    public static boolean isPipeline(@Nullable final Step step)
    {
        return step != null && step.isCompoundStep() && !XProcSteps.ALL_STEPS.contains(step.getType())
                && !XProcSteps.PIPELINE.equals(step.getType());
    }
}
