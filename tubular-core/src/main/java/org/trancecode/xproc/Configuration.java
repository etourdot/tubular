/*
 * Copyright (C) 2007 TranceCode Software
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
package org.trancecode.xproc;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.io.DefaultInputResolver;
import org.trancecode.io.DefaultOutputResolver;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.step.ChooseStepProcessor;
import org.trancecode.xproc.step.ForEachStepProcessor;
import org.trancecode.xproc.step.GroupStepProcessor;
import org.trancecode.xproc.step.Step;
import org.trancecode.xproc.step.StepProcessor;
import org.trancecode.xproc.step.WhenStepProcessor;
import org.trancecode.xproc.step.XProcSteps;

/**
 * @author Herve Quiroz
 */
public final class Configuration implements PipelineContext
{
    private static final String RESOURCE_PATH_XPROC_LIBRARY_1_0 = "/org/trancecode/xproc/xproc-1.0.xpl";
    private static final Map<QName, StepProcessor> DEFAULT_STEP_PROCESSORS = getDefaultStepProcessors();
    private static final Map<QName, Step> CORE_LIBRARY = getCoreLibrary();
    private static final URI DEFAULT_LIBRARY_URI = URI.create("trancecode:tubular:default-library.xpl");
    private static final Set<URI> EMPTY_SET_OF_URIS = ImmutableSet.of();

    private ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private URIResolver uriResolver;
    private OutputResolver outputResolver = DefaultOutputResolver.INSTANCE;
    private InputResolver inputResolver = DefaultInputResolver.INSTANCE;
    private PipelineLibrary library = getDefaultLPipelineLibrary();
    private final Processor processor;
    private final Map<QName, StepProcessor> stepProcessors = Maps.newHashMap(DEFAULT_STEP_PROCESSORS);

    private static PipelineLibrary getDefaultLPipelineLibrary()
    {
        final PipelineContext context = new PipelineContext()
        {
            private final Processor processor = new Processor(false);
            private final PipelineLibrary library = new PipelineLibrary(DEFAULT_LIBRARY_URI, CORE_LIBRARY,
                    EMPTY_SET_OF_URIS);

            @Override
            public ExecutorService getExecutorService()
            {
                return Executors.newSingleThreadScheduledExecutor();
            }

            @Override
            public InputResolver getInputResolver()
            {
                return null;
            }

            @Override
            public OutputResolver getOutputResolver()
            {
                return null;
            }

            @Override
            public Processor getProcessor()
            {
                return processor;
            }

            @Override
            public URIResolver getUriResolver()
            {
                return processor.getUnderlyingConfiguration().getURIResolver();
            }

            @Override
            public StepProcessor getStepProcessor(final QName step)
            {
                return DEFAULT_STEP_PROCESSORS.get(step);
            }

            @Override
            public Map<QName, StepProcessor> getStepProcessors()
            {
                return DEFAULT_STEP_PROCESSORS;
            }

            @Override
            public PipelineLibrary getPipelineLibrary()
            {
                return library;
            }
        };
        final URL xprocLibraryUrl = Configuration.class.getResource(RESOURCE_PATH_XPROC_LIBRARY_1_0);
        Source defaultLibrarySource;
        try
        {
            defaultLibrarySource = new StreamSource(xprocLibraryUrl.openStream(), xprocLibraryUrl.toString());
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("cannot parse default library: " + xprocLibraryUrl, e);
        }
        final PipelineParser parser = new PipelineParser(context, defaultLibrarySource);
        parser.parse();

        return parser.getLibrary();
    }

    private static Map<QName, StepProcessor> getDefaultStepProcessors()
    {
        final Iterable<StepProcessor> processors = ServiceLoader.load(StepProcessor.class);
        return ImmutableMap.copyOf(Maps.uniqueIndex(processors, new Function<StepProcessor, QName>()
        {
            @Override
            public QName apply(final StepProcessor stepProcessor)
            {
                return stepProcessor.stepType();
            }
        }));
    }

    private static Map<QName, Step> getCoreLibrary()
    {
        final Map<QName, Step> coreSteps = Maps.newHashMap();

        coreSteps.put(XProcSteps.CHOOSE, ChooseStepProcessor.STEP);
        coreSteps.put(XProcSteps.FOR_EACH, ForEachStepProcessor.STEP);
        coreSteps.put(XProcSteps.GROUP, GroupStepProcessor.STEP);
        coreSteps.put(XProcSteps.OTHERWISE, WhenStepProcessor.STEP_OTHERWISE);
        coreSteps.put(XProcSteps.WHEN, WhenStepProcessor.STEP_WHEN);
        // TODO coreSteps.put(XProcSteps.TRY, null);

        return ImmutableMap.copyOf(coreSteps);
    }

    public Configuration()
    {
        this(new Processor(false));
    }

    public Configuration(final Processor processor)
    {
        this.processor = Preconditions.checkNotNull(processor);
        uriResolver = processor.getUnderlyingConfiguration().getURIResolver();
    }

    @Override
    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    public void setExecutorService(final ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    @Override
    public InputResolver getInputResolver()
    {
        return this.inputResolver;
    }

    public void setInputResolver(final InputResolver inputResolver)
    {
        this.inputResolver = Preconditions.checkNotNull(inputResolver);
    }

    @Override
    public URIResolver getUriResolver()
    {
        return uriResolver;
    }

    public void setUriResolver(final URIResolver uriResolver)
    {
        this.uriResolver = Preconditions.checkNotNull(uriResolver);
    }

    @Override
    public OutputResolver getOutputResolver()
    {
        return this.outputResolver;
    }

    public void setOutputResolver(final OutputResolver outputResolver)
    {
        this.outputResolver = Preconditions.checkNotNull(outputResolver);
    }

    @Override
    public Processor getProcessor()
    {
        return processor;
    }

    public void registerStepProcessor(final StepProcessor stepProcessor)
    {
        Preconditions.checkNotNull(stepProcessor);
        stepProcessors.put(stepProcessor.stepType(), stepProcessor);
    }

    public void registerPipelineLibrary(final PipelineLibrary library)
    {
        Preconditions.checkNotNull(library);
        this.library = library.importLibrary(this.library);
    }

    @Override
    public PipelineLibrary getPipelineLibrary()
    {
        return library;
    }

    @Override
    public StepProcessor getStepProcessor(final QName step)
    {
        if (stepProcessors.containsKey(step))
        {
            return stepProcessors.get(step);
        }

        throw new NoSuchElementException(step.toString());
    }

    @Override
    public Map<QName, StepProcessor> getStepProcessors()
    {
        return Collections.unmodifiableMap(stepProcessors);
    }
}
