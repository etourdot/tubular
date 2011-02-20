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

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcXmlModel;

public class HttpResponseHandler implements ResponseHandler<XProcHttpResponse>
{
    private final boolean detailled;
    private final boolean statusOnly;
    private final Processor processor;

    public HttpResponseHandler(final Processor processor, final boolean detailled, final boolean statusOnly)
    {
        this.processor = processor;
        this.detailled = detailled;
        this.statusOnly = statusOnly;
    }

    @Override
    public XProcHttpResponse handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException
    {
        final XProcHttpResponse response = new XProcHttpResponse();
        if (!detailled)
        {
            if (!statusOnly)
            {
                try
                {
                    final InputStream responseStream = httpResponse.getEntity().getContent();
                    final String contentMimeType = EntityUtils.getContentMimeType(httpResponse.getEntity());
                    final String contentCharset = EntityUtils.getContentCharSet(httpResponse.getEntity());
                    if (contentMimeType.endsWith("xml"))
                    {
                        response.setNodes(ImmutableList.of(processor.newDocumentBuilder().build(new StreamSource(responseStream))));
                    }
                    else
                    {
                        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
                        builder.startDocument();
                        builder.startElement(XProcXmlModel.Elements.BODY);
                        builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, httpResponse.getEntity().getContentType().getValue());
                        builder.startContent();
                        builder.text(EntityUtils.toString(httpResponse.getEntity()));
                        builder.endDocument();
                        response.setNodes(ImmutableList.of(builder.getNode()));
                    }
                }
                catch (SaxonApiException sae)
                {
                    throw new ClientProtocolException(sae);
                }
            }
        }
        return response;
    }
}