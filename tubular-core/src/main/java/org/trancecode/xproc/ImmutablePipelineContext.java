/*
 * Copyright (C) 2010 TranceCode Software
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

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;

/**
 * @author Herve Quiroz
 */
public final class ImmutablePipelineContext implements PipelineContext
{
    private final InputResolver inputResolver;
    private final OutputResolver outputResolver;
    private final Processor processor;
    private final URIResolver uriResolver;

    public static ImmutablePipelineContext copyOf(final PipelineContext context)
    {
        if (context instanceof ImmutablePipelineContext)
        {
            return (ImmutablePipelineContext) context;
        }

        return new ImmutablePipelineContext(context.getProcessor(), context.getInputResolver(),
                context.getOutputResolver(), context.getUriResolver());
    }

    private ImmutablePipelineContext(final Processor processor, final InputResolver inputResolver,
            final OutputResolver outputResolver, final URIResolver uriResolver)
    {
        this.inputResolver = inputResolver;
        this.outputResolver = outputResolver;
        this.processor = processor;
        this.uriResolver = uriResolver;
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
    public Processor getProcessor()
    {
        return processor;
    }

    @Override
    public URIResolver getUriResolver()
    {
        return uriResolver;
    }
}
