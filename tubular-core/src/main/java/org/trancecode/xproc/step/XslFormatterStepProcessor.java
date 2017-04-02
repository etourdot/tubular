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
package org.trancecode.xproc.step;

import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.trancecode.io.Uris;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Herve Quiroz
 */
@ExternalResources(read = false, write = true)
public final class XslFormatterStepProcessor extends AbstractStepProcessor
{
    public static final String DEFAULT_CONTENT_TYPE = "application/pdf";

    private static final Logger LOG = Logger.getLogger(XslFormatterStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.XSL_FORMATTER;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output) throws Exception
    {
        LOG.trace("{@method}");

        final XdmNode source = input.readNode(XProcPorts.SOURCE);

        final URI baseUri = source.getBaseURI();
        final String href = input.getOptionValue(XProcOptions.HREF, null);
        assert href != null;
        final OutputStream resultOutputStream = input.getPipelineContext().getOutputResolver()
                .resolveOutputStream(href, baseUri.toString());
        try
        {
            final String contentType = input.getOptionValue(XProcOptions.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            final FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(baseUri, new ResourceResolver() {
                @Override
                public Resource getResource(URI uri) throws IOException {
                    final URI resolvedUri = Uris.resolve(uri, baseUri);
                    final InputStream inputStream = input.getPipelineContext().getInputResolver()
                      .resolveInputStream(resolvedUri);
                    return new Resource(inputStream);
                }

                @Override
                public OutputStream getOutputStream(URI uri) throws IOException {
                    return null;
                }
            });
            final FopFactory fopFactory = fopFactoryBuilder.build();
            final Fop fop = fopFactory.newFop(contentType, resultOutputStream);
            /*fop.getUserAgent().setURIResolver((href1, base) -> {
                final URI uri = Uris.resolve(href1, base);
                final InputStream inputStream = input.getPipelineContext().getInputResolver()
                        .resolveInputStream(href1, base);
                return new StreamSource(inputStream, uri.toString());
            });
            fop.getUserAgent().setBaseURL(source.getBaseURI().toString());*/

            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer();

            final SAXResult fopResult = new SAXResult(fop.getDefaultHandler());

            transformer.transform(source.asSource(), fopResult);
        }
        finally
        {
            Closeables.closeQuietly(resultOutputStream);
        }

        output.writeNodes(XProcPorts.RESULT, input.newResultElement(Uris.resolve(href, baseUri.toString()).toString()));
    }
}
