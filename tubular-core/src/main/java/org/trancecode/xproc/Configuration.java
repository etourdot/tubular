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

import com.google.common.base.Preconditions;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import org.trancecode.io.DefaultInputResolver;
import org.trancecode.io.DefaultOutputResolver;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.step.StepProcessor;

/**
 * @author Herve Quiroz
 */
public final class Configuration implements PipelineContext
{
    private URIResolver uriResolver;
    private OutputResolver outputResolver = DefaultOutputResolver.INSTANCE;
    private InputResolver inputResolver = DefaultInputResolver.INSTANCE;
    private final Processor processor;

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
        // TODO
        throw new UnsupportedOperationException();
    }

    public void registerPipelineLibrary(final PipelineLibrary library)
    {
        // TODO
        throw new UnsupportedOperationException();
    }
}
