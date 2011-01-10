/*
 * Copyright (C) 2010 Herve Quiroz
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
 */
package org.trancecode.xproc;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.concurrent.TaskExecutor;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.step.StepProcessor;

/**
 * @author Herve Quiroz
 */
public final class ImmutablePipelineContext implements PipelineContext
{
    private final TaskExecutor executor;
    private final InputResolver inputResolver;
    private final OutputResolver outputResolver;
    private final PipelineLibrary pipelineLibrary;
    private final Processor processor;
    private final Map<QName, StepProcessor> stepProcessors;
    private final URIResolver uriResolver;

    public static ImmutablePipelineContext copyOf(final PipelineContext context)
    {
        if (context instanceof ImmutablePipelineContext)
        {
            return (ImmutablePipelineContext) context;
        }

        return new ImmutablePipelineContext(context.getProcessor(), context.getExecutor(), context.getInputResolver(),
                context.getOutputResolver(), context.getUriResolver(), context.getStepProcessors(),
                context.getPipelineLibrary());
    }

    private ImmutablePipelineContext(final Processor processor, final TaskExecutor executor,
            final InputResolver inputResolver, final OutputResolver outputResolver, final URIResolver uriResolver,
            final Map<QName, StepProcessor> stepProcessors, final PipelineLibrary pipelineLibrary)
    {
        this.executor = executor;
        this.inputResolver = inputResolver;
        this.outputResolver = outputResolver;
        this.pipelineLibrary = pipelineLibrary;
        this.processor = processor;
        this.uriResolver = uriResolver;
        this.stepProcessors = ImmutableMap.copyOf(stepProcessors);
    }

    @Override
    public TaskExecutor getExecutor()
    {
        return executor;
    }

    @Override
    public InputResolver getInputResolver()
    {
        return inputResolver;
    }

    @Override
    public OutputResolver getOutputResolver()
    {
        return outputResolver;
    }

    @Override
    public PipelineLibrary getPipelineLibrary()
    {
        return pipelineLibrary;
    }

    @Override
    public Processor getProcessor()
    {
        return processor;
    }

    @Override
    public URIResolver getUriResolver()
    {
        return uriResolver;
    }

    @Override
    public StepProcessor getStepProcessor(final QName step)
    {
        if (stepProcessors.containsKey(step))
        {
            return stepProcessors.get(step);
        }

        // TODO XProc error?
        throw new NoSuchElementException(step.toString());
    }

    @Override
    public Map<QName, StepProcessor> getStepProcessors()
    {
        return stepProcessors;
    }
}
