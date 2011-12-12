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
package org.trancecode.xproc.jaxproc;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.trancecode.xproc.RunnablePipeline;
import org.trancecode.xproc.api.Pipeline;
import org.trancecode.xproc.api.PipelineResult;

/**
 * @author Herve Quiroz
 */
public final class TubularPipeline extends Pipeline
{
    private final RunnablePipeline pipeline;
    private final Map<String, Result> outputPortBindings = new HashMap<String, Result>();

    TubularPipeline(final RunnablePipeline pipeline)
    {
        this.pipeline = Preconditions.checkNotNull(pipeline);
    }

    @Override
    public void setParameter(final QName name, final Object value)
    {
        pipeline.withParam(name, value.toString());
    }

    @Override
    public void setOption(final QName name, final Object value)
    {
        pipeline.withOption(name, value.toString());
    }

    @Override
    public void bindInputPort(final String name, final Iterable<Source> sources)
    {
        bindInputPort(name, sources);
    }

    @Override
    public void bindOutputPort(final String name, final Result result)
    {
        outputPortBindings.put(name, result);
    }

    @Override
    public PipelineResult execute()
    {
        final org.trancecode.xproc.PipelineResult result = pipeline.run();

        for (final Entry<String, Result> outputPortBinding : outputPortBindings.entrySet())
        {
            result.readNode(outputPortBinding.getKey(), outputPortBinding.getValue());
        }

        return new TubularPipelineResult(result);
    }
}
