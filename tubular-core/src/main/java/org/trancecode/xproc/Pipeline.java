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

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;

/**
 * @author Herve Quiroz
 */
public class Pipeline
{
    private final Processor processor;
    private final URIResolver uriResolver;
    private final Step pipeline;

    protected Pipeline(final Processor processor, final URIResolver uriResolver, final Step pipeline)
    {
        assert processor != null;
        this.processor = processor;

        assert uriResolver != null;
        this.uriResolver = uriResolver;

        assert pipeline != null;
        this.pipeline = pipeline;
    }

    public RunnablePipeline load()
    {
        final RunnablePipeline pipeline = new RunnablePipeline(this);
        pipeline.setUriResolver(uriResolver);

        return pipeline;
    }

    protected Step getUnderlyingPipeline()
    {
        return pipeline;
    }

    public Processor getProcessor()
    {
        return processor;
    }
}
