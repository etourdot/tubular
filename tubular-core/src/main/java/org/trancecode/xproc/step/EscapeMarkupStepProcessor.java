/*
 * Copyright (C) 2011 Emmanuel Tourdot
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
package org.trancecode.xproc.step;

import com.google.common.io.Closeables;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.io.MediaTypes;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

import java.io.ByteArrayOutputStream;

/**
 * @author Emmanuel Tourdot
 */
public final class EscapeMarkupStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(EscapeMarkupStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.ESCAPE_MARKUP;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode node = input.readNode(XProcPorts.SOURCE);

        final String mimeType = input.getOptionValue(XProcOptions.MEDIA_TYPE, MediaTypes.MEDIA_TYPE_XML);
        final String doctypePublicId = input.getOptionValue(XProcOptions.DOCTYPE_PUBLIC, null);
        final String doctypeSystemId = input.getOptionValue(XProcOptions.DOCTYPE_SYSTEM, null);
        final String method = input.getOptionValue(XProcOptions.METHOD, "xml");
        final boolean escapeUri = Boolean.parseBoolean(input
                .getOptionValue(XProcOptions.ESCAPE_URI_ATTRIBUTES, "false"));
        final boolean omitXML = Boolean.parseBoolean(input.getOptionValue(XProcOptions.OMIT_XML_DECLARATION, "true"));
        final boolean indent = Boolean.parseBoolean(input.getOptionValue(XProcOptions.INDENT, "false"));
        final boolean includeContent = Boolean.parseBoolean(input.getOptionValue(XProcOptions.INCLUDE_CONTENT_TYPE,
                "true"));

        final XdmNode root = SaxonAxis.childElement(node);
        final ByteArrayOutputStream targetOutputStream = new ByteArrayOutputStream();
        final Serializer serializer = new Serializer();
        serializer.setOutputStream(targetOutputStream);
        if (doctypePublicId != null)
        {
            serializer.setOutputProperty(Property.DOCTYPE_PUBLIC, doctypePublicId);
        }
        if (doctypeSystemId != null)
        {
            serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
        }
        serializer.setOutputProperty(Property.METHOD, method);
        serializer.setOutputProperty(Property.ESCAPE_URI_ATTRIBUTES, (escapeUri ? "yes" : "no"));
        serializer.setOutputProperty(Property.MEDIA_TYPE, mimeType);
        serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, (omitXML ? "yes" : "no"));
        serializer.setOutputProperty(Property.INDENT, (indent ? "yes" : "no"));
        serializer.setOutputProperty(Property.INCLUDE_CONTENT_TYPE, (includeContent ? "yes" : "no"));

        try
        {
            input.getPipelineContext().getProcessor().writeXdmValue(SaxonAxis.childElement(root), serializer);
            targetOutputStream.close();
        }
        catch (final Exception e)
        {
            throw new PipelineException("Error while trying to write document", e);
        }
        finally
        {
            Closeables.closeQuietly(targetOutputStream);
        }

        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(root.getNodeName());
        builder.startContent();
        builder.text(new String(targetOutputStream.toByteArray()));
        builder.endElement();
        builder.endDocument();
        output.writeNodes(XProcPorts.RESULT, builder.getNode());
    }
}
