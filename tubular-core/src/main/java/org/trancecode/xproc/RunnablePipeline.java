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
package org.trancecode.xproc;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.io.OutputResolver;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonFunctions;
import org.trancecode.xproc.binding.PortBinding;
import org.trancecode.xproc.binding.PortBindingFunctions;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public class RunnablePipeline
{
    private static final Logger LOG = Logger.getLogger(RunnablePipeline.class);

    private final Processor processor;
    private URIResolver uriResolver;
    private OutputResolver outputResolver;
    private Step pipeline;

    protected RunnablePipeline(final Pipeline pipeline)
    {
        assert pipeline != null;
        this.pipeline = pipeline.getUnderlyingPipeline();
        processor = pipeline.getProcessor();
    }

    public PipelineResult run()
    {
        LOG.trace("pipeline = {}", pipeline);

        final Configuration configuration = new Configuration(processor);
        configuration.setOutputResolver(outputResolver);
        configuration.setUriResolver(uriResolver);
        final Environment environment = Environment.newEnvironment(pipeline, configuration);

        final Environment resultEnvironment = pipeline.run(environment);

        return new PipelineResult(pipeline, resultEnvironment);
    }

    public void withParam(final QName name, final String value)
    {
        LOG.trace("name = {} ; value = {}", name, value);
        pipeline = pipeline.withParam(name, null, value, pipeline.getLocation());
    }

    public void withOption(final QName name, final String value)
    {
        LOG.trace("name = {} ; value = {}", name, value);
        pipeline = pipeline.withOptionValue(name, value);
    }

    public void setPortBinding(final String portName, final Iterable<Source> bindings)
    {
        final List<PortBinding> portBindings = ImmutableList.copyOf(Iterables.transform(bindings,
                Functions.compose(PortBindingFunctions.toPortBinding(), SaxonFunctions.buildDocument(processor))));

        pipeline = pipeline.setPortBindings(portName, portBindings);
    }

    public void setPortBinding(final String portName, final Source... bindings)
    {
        setPortBinding(portName, ImmutableList.copyOf(bindings));
    }

    public void setUriResolver(final URIResolver uriResolver)
    {
        this.uriResolver = uriResolver;
    }

    public void setOutputResolver(final OutputResolver outputResolver)
    {
        this.outputResolver = outputResolver;
    }

    public Step getPipeline()
    {
        return pipeline;
    }
}
