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

import javax.xml.transform.Source;

import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public final class PipelineProcessor
{
    private final PipelineContext context;

    public PipelineProcessor()
    {
        this(new PipelineConfiguration());
    }

    public PipelineProcessor(final PipelineConfiguration configuration)
    {
        this((PipelineContext) configuration);
    }

    PipelineProcessor(final PipelineContext context)
    {
        this.context = ImmutablePipelineContext.copyOf(context);
    }

    public PipelineContext getPipelineContext()
    {
        return context;
    }

    public PipelineLibrary buildPipelineLibrary(final Source source)
    {
        return PipelineParser.parseLibrary(context, source);
    }

    public Pipeline buildPipeline(final Source source)
    {
        final Step pipelineStep = PipelineParser.parsePipeline(context, source);
        return new Pipeline(context, pipelineStep);
    }
}
