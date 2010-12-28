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
package org.trancecode.xproc.step;

import com.google.common.io.Closeables;

import java.io.OutputStream;
import java.net.URI;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.io.MediaTypes;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Herve Quiroz
 */
public final class StoreStepProcessor extends AbstractStepProcessor
{
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_OMIT_XML_DECLARATION = "no";
    private static final String DEFAULT_DOCTYPE_PUBLIC = null;
    private static final String DEFAULT_DOCTYPE_SYSTEM = null;
    private static final String DEFAULT_METHOD = null;
    private static final String DEFAULT_MIMETYPE = MediaTypes.MEDIA_TYPE_XML;

    private static final Logger LOG = Logger.getLogger(StoreStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.STORE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode node = input.readNode(XProcPorts.SOURCE);
        assert node != null;

        final URI baseUri = input.getBaseUri();
        final String providedHref = input.getOptionValue(XProcOptions.HREF);
        final String href;
        if (providedHref != null)
        {
            href = providedHref;
        }
        else
        {
            href = node.getUnderlyingNode().getSystemId();
        }

        final URI outputUri = baseUri.resolve(href);

        final String mimeType = input.getOptionValue(XProcOptions.MEDIA_TYPE, DEFAULT_MIMETYPE);
        final String encoding = input.getOptionValue(XProcOptions.ENCODING, DEFAULT_ENCODING);
        final String omitXmlDeclaration = input.getOptionValue(XProcOptions.OMIT_XML_DECLARATION,
                DEFAULT_OMIT_XML_DECLARATION);
        final String doctypePublicId = input.getOptionValue(XProcOptions.DOCTYPE_PUBLIC, DEFAULT_DOCTYPE_PUBLIC);
        final String doctypeSystemId = input.getOptionValue(XProcOptions.DOCTYPE_SYSTEM, DEFAULT_DOCTYPE_SYSTEM);
        final String method = input.getOptionValue(XProcOptions.METHOD, DEFAULT_METHOD);
        final boolean indent = Boolean.parseBoolean(input.getOptionValue(XProcOptions.INDENT));

        LOG.debug("Storing document to: {} ; mime-type: {} ; encoding: {} ; doctype-public = {} ; doctype-system = {}",
                href, mimeType, encoding, doctypePublicId, doctypeSystemId);

        final OutputStream targetOutputStream = input.getPipelineContext().getOutputResolver()
                .resolveOutputStream(href, input.getBaseUri().toString());

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
        serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
        if (method != null)
        {
            LOG.debug("method = {}", method);
            serializer.setOutputProperty(Property.METHOD, method);
        }
        serializer.setOutputProperty(Property.ENCODING, encoding);
        serializer.setOutputProperty(Property.MEDIA_TYPE, mimeType);
        serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, omitXmlDeclaration);
        serializer.setOutputProperty(Property.INDENT, (indent ? "yes" : "no"));

        try
        {
            input.getPipelineContext().getProcessor().writeXdmValue(node, serializer);
            targetOutputStream.close();
        }
        catch (final Exception e)
        {
            throw new PipelineException("Error while trying to write document ; output-base-uri = %s", e, outputUri);
        }
        finally
        {
            Closeables.closeQuietly(targetOutputStream);
        }

        output.writeNodes(XProcPorts.RESULT, input.newResultElement(outputUri.toString()));
    }
}
