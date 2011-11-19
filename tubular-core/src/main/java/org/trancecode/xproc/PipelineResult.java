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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.io.RuntimeIOException;
import org.trancecode.xml.XmlException;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public final class PipelineResult
{
    private final Step pipeline;
    private final Environment resultEnvironment;

    protected PipelineResult(final Step pipeline, final Environment resultEnvironment)
    {
        this.pipeline = Preconditions.checkNotNull(pipeline);
        this.resultEnvironment = Preconditions.checkNotNull(resultEnvironment);
    }

    public Step getPipeline()
    {
        return pipeline;
    }

    public XdmNode readNode(final String portName)
    {
        return Iterables.getOnlyElement(readNodes(portName));
    }

    public Iterable<XdmNode> readNodes(final String portName)
    {
        Preconditions.checkNotNull(portName);
        final Port declaredPort = pipeline.getPort(portName);
        final EnvironmentPort environmentPort = resultEnvironment.getEnvironmentPort(declaredPort);
        return environmentPort.readNodes();
    }

    public void readNode(final String portName, final File outputFile)
    {
        Preconditions.checkNotNull(outputFile);
        final XdmNode node = Iterables.getOnlyElement(readNodes(portName));
        try
        {
            Files.write(node.toString(), outputFile, Charset.defaultCharset());
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public void readNode(final String portName, final Result result)
    {
        Preconditions.checkNotNull(result);
        final XdmNode node = Iterables.getOnlyElement(readNodes(portName));
        try
        {
            TransformerFactoryImpl.newInstance().newTransformer().transform(node.asSource(), result);
        }
        catch (final TransformerException e)
        {
            throw new XmlException(e, "cannot push node from port '%' to result", portName);
        }
    }
}
