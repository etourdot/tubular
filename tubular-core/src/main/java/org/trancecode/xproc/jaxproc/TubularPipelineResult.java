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

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xproc.api.PipelineResult;

/**
 * @author Herve Quiroz
 */
public final class TubularPipelineResult extends PipelineResult
{
    private final org.trancecode.xproc.PipelineResult pipelineResult;

    TubularPipelineResult(final org.trancecode.xproc.PipelineResult result)
    {
        this.pipelineResult = result;
    }

    @Override
    public void readDocument(final String portName, final Result result)
    {
        pipelineResult.readNode(portName, result);
    }

    @Override
    public Source readDocument(final String portName)
    {
        return pipelineResult.readNode(portName).asSource();
    }

    @Override
    public Iterable<Source> readDocuments(final String portName)
    {
        return Saxon.asSources(pipelineResult.readNodes(portName));
    }
}
