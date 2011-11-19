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
package org.trancecode.xproc;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xml.UriResolvers;
import org.trancecode.xml.saxon.SaxonFunctions;
import org.trancecode.xml.saxon.SaxonQNames;
import org.trancecode.xproc.binding.PortBinding;
import org.trancecode.xproc.binding.PortBindingFunctions;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public class RunnablePipeline
{
    private static final Logger LOG = Logger.getLogger(RunnablePipeline.class);

    private final PipelineContext context;
    private Step pipeline;

    protected RunnablePipeline(final Pipeline pipeline)
    {
        Preconditions.checkNotNull(pipeline);
        this.context = ImmutablePipelineContext.copyOf(pipeline.getPipelineContext()).withNewEpisode();
        this.pipeline = pipeline.getUnderlyingPipeline();
    }

    public PipelineResult run()
    {
        LOG.trace("{@method} pipeline = {}", pipeline);
        final Environment environment = Environment.newEnvironment(pipeline, context);
        final Environment resultEnvironment = pipeline.run(environment);
        return new PipelineResult(pipeline, resultEnvironment);
    }

    public void withParam(final javax.xml.namespace.QName name, final String value)
    {
        withParam(SaxonQNames.asSaxonQName(name), value);
    }

    public void withParam(final QName name, final String value)
    {
        LOG.trace("{@method} name = {} ; value = {}", name, value);
        pipeline = pipeline.withParam(name, null, value, pipeline.getLocation());
    }

    public void withOption(final javax.xml.namespace.QName name, final String value)
    {
        withOption(SaxonQNames.asSaxonQName(name), value);
    }

    public void withOption(final QName name, final String value)
    {
        LOG.trace("{@method} name = {} ; value = {}", name, value);
        pipeline = pipeline.withOptionValue(name, value);
    }

    public void bindSourcePortToResources(final String portName, final Iterable<URI> inputResources)
    {
        final Iterable<Source> sources = Iterables.transform(inputResources, new Function<URI, Source>()
        {
            @Override
            public Source apply(final URI resource)
            {
                return UriResolvers.resolve(context.getUriResolver(), resource, pipeline.getNode().getBaseURI());
            }
        });
        bindSourcePort(portName, sources);
    }

    public void bindSourcePortToResources(final String portName, final URI... inputResources)
    {
        bindSourcePortToResources(portName, ImmutableList.copyOf(inputResources));
    }

    public void bindSourcePortToFiles(final String portName, final Iterable<File> inputFiles)
    {
        final Iterable<Source> sources = Iterables.transform(inputFiles, new Function<File, Source>()
        {
            @Override
            public Source apply(final File file)
            {
                return new StreamSource(file);
            }
        });
        bindSourcePort(portName, sources);
    }

    public void bindSourcePortToFiles(final String portName, final File... inputFiles)
    {
        bindSourcePortToFiles(portName, ImmutableList.copyOf(inputFiles));
    }

    public void bindSourcePort(final String portName, final Iterable<Source> bindings)
    {
        final List<PortBinding> portBindings = ImmutableList.copyOf(Iterables.transform(
                bindings,
                Functions.compose(PortBindingFunctions.toPortBinding(),
                        SaxonFunctions.buildDocument(context.getProcessor()))));

        pipeline = pipeline.setPortBindings(portName, portBindings);
    }

    public void bindSourcePort(final String portName, final Source... bindings)
    {
        bindSourcePort(portName, ImmutableList.copyOf(bindings));
    }

    public Step getPipeline()
    {
        return pipeline;
    }
}
