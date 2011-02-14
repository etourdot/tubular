/*
 * Copyright (C) 2011 Herve Quiroz
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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.concurrent.TaskExecutor;
import org.trancecode.function.TcSuppliers;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.step.StepProcessor;

/**
 * @author Herve Quiroz
 */
abstract class AbstractPipelineContext implements PipelineContext
{
    final static String PROPERTY_PREFIX = "http://www.trancecode.org/tubular/1/property/";

    final static String PROPERTY_EPISODE = PROPERTY_PREFIX + "episode";
    final static String PROPERTY_EXECUTOR = PROPERTY_PREFIX + "executor";
    final static String PROPERTY_INPUT_RESOLVER = PROPERTY_PREFIX + "inputResolver";
    final static String PROPERTY_OUTPUT_RESOLVER = PROPERTY_PREFIX + "outputResolver";
    final static String PROPERTY_PIPELINE_LIBRARY = PROPERTY_PREFIX + "pipelineLibrary";
    final static String PROPERTY_PROCESSOR = PROPERTY_PREFIX + "processor";
    final static String PROPERTY_STEP_PROCESSORS = PROPERTY_PREFIX + "stepProcessors";
    final static String PROPERTY_URI_RESOLVER = PROPERTY_PREFIX + "uriResolver";

    final Map<String, Object> properties;

    Supplier<Episode> episode;
    Supplier<TaskExecutor> executor;
    Supplier<InputResolver> inputResolver;
    Supplier<OutputResolver> outputResolver;
    Supplier<PipelineLibrary> pipelineLibrary;
    Supplier<Processor> processor;
    Supplier<Map<QName, StepProcessor>> stepProcessors;
    Supplier<URIResolver> uriResolver;

    protected AbstractPipelineContext(final Map<String, Object> properties)
    {
        this.properties = Preconditions.checkNotNull(properties);
        episode = TcSuppliers.getFromMap(properties, PROPERTY_EPISODE);
        executor = TcSuppliers.getFromMap(properties, PROPERTY_EXECUTOR);
        inputResolver = TcSuppliers.getFromMap(properties, PROPERTY_INPUT_RESOLVER);
        outputResolver = TcSuppliers.getFromMap(properties, PROPERTY_OUTPUT_RESOLVER);
        pipelineLibrary = TcSuppliers.getFromMap(properties, PROPERTY_PIPELINE_LIBRARY);
        processor = TcSuppliers.getFromMap(properties, PROPERTY_PROCESSOR);
        stepProcessors = TcSuppliers.getFromMap(properties, PROPERTY_STEP_PROCESSORS);
        uriResolver = TcSuppliers.getFromMap(properties, PROPERTY_URI_RESOLVER);
    }

    @Override
    public final <T> T getProperty(final String name)
    {
        @SuppressWarnings("unchecked")
        final T value = (T) properties.get(name);
        return value;
    }

    final Map<String, Object> getProperties()
    {
        return properties;
    }

    @Override
    public final Episode getEpisode()
    {
        return episode.get();
    }

    @Override
    public final TaskExecutor getExecutor()
    {
        return executor.get();
    }

    @Override
    public final InputResolver getInputResolver()
    {
        return inputResolver.get();
    }

    @Override
    public final OutputResolver getOutputResolver()
    {
        return outputResolver.get();
    }

    @Override
    public final PipelineLibrary getPipelineLibrary()
    {
        return pipelineLibrary.get();
    }

    @Override
    public final Processor getProcessor()
    {
        return processor.get();
    }

    @Override
    public final URIResolver getUriResolver()
    {
        return uriResolver.get();
    }

    @Override
    public final StepProcessor getStepProcessor(final QName step)
    {
        if (stepProcessors.get().containsKey(step))
        {
            return stepProcessors.get().get(step);
        }

        // TODO XProc error?
        throw new NoSuchElementException(step.toString());
    }

    @Override
    public final Map<QName, StepProcessor> getStepProcessors()
    {
        return Collections.unmodifiableMap(stepProcessors.get());
    }
}
