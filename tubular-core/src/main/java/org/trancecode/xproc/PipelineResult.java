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

import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmNode;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public class PipelineResult
{
    private final Step pipeline;
    private final Environment resultEnvironment;

    protected PipelineResult(final Step pipeline, final Environment resultEnvironment)
    {
        assert pipeline != null;
        this.pipeline = pipeline;

        assert resultEnvironment != null;
        this.resultEnvironment = resultEnvironment;
    }

    public Step getPipeline()
    {
        return pipeline;
    }

    public List<Source> readPort(final String stepName, final String portName)
    {
        // TODO
        return null;
    }

    public Iterable<XdmNode> readNodes(final String portName)
    {
        final Port declaredPort = pipeline.getPort(portName);
        final EnvironmentPort environmentPort = resultEnvironment.getEnvironmentPort(declaredPort);
        return environmentPort.readNodes();
    }
}
