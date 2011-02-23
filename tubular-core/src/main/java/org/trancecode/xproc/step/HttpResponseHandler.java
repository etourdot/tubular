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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.util.EntityUtils;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcXmlModel;

public class HttpResponseHandler implements ResponseHandler<XProcHttpResponse>
{
    private final boolean detailed;
    private final boolean statusOnly;
    private final Processor processor;

    public HttpResponseHandler(final Processor processor, final boolean detailed, final boolean statusOnly)
    {
        this.processor = processor;
        this.detailed = detailed;
        this.statusOnly = statusOnly;
    }

    @Override
    public XProcHttpResponse handleResponse(final HttpResponse httpResponse) throws IOException
    {
        final XProcHttpResponse response = new XProcHttpResponse();
        final HttpEntity entity = httpResponse.getEntity();
        final String contentCharset = EntityUtils.getContentCharSet(entity) == null ? "utf-8" : EntityUtils.getContentCharSet(entity);
        final String contentType = constructContentType(entity.getContentType());
        ContentType contentMimeType = null;
        try
        {
            contentMimeType = new ContentType(contentType);
        }
        catch (ParseException e)
        {
            contentMimeType = new ContentType("text", "plain", null);
        }
        if (!detailed)
        {
            if (!statusOnly)
            {
                if ("multipart".equals(contentMimeType.getPrimaryType()))
                {
                    response.setNodes(constructMultipart(entity, contentMimeType, contentType));
                }
                else
                {
                    final InputStream entityStream = entity.getContent();
                    final Iterable<XdmNode> body = constructBody(entityStream, contentMimeType, contentType);
                    if (body != null)
                    {
                        response.setNodes(body);
                    }
                    entityStream.close();
                }
                EntityUtils.consume(entity);
            }
        }
        else
        {
            final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
            builder.startDocument();
            builder.startElement(XProcXmlModel.Elements.RESPONSE);
            builder.attribute(XProcXmlModel.Attributes.STATUS, Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            final InputStream entityStream = entity.getContent();
            final Iterable<XdmNode> body = constructBody(entityStream, contentMimeType, contentType);
            if (body != null)
            {
                builder.nodes(body);
            }
            entityStream.close();
            builder.endDocument();
            response.setNodes(ImmutableList.of(builder.getNode()));
        }
        return response;
    }

    private Iterable<XdmNode> constructBody(final InputStream entityStream, final ContentType contentMimeType, final String contentType) throws IOException
    {
        if (contentMimeType.getSubType().contains("xml"))
        {
            try
            {
                final XdmNode node = processor.newDocumentBuilder().build(new StreamSource(entityStream));
                return ImmutableList.of(node);
            }
            catch (SaxonApiException sae)
            {
            }
        }
        else
        {
            final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
            builder.startDocument();
            builder.startElement(XProcXmlModel.Elements.BODY);
            builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, contentType);
            if ("text".equals(contentMimeType.getPrimaryType()))
            {
                builder.startContent();
                builder.text(IOUtils.toString(entityStream));
                builder.endDocument();
            }
            else
            {
                builder.attribute(XProcXmlModel.Attributes.ENCODING, "base64");
                builder.startContent();
                //TODO: optimization
                final String b64 = Base64.encodeBase64String(IOUtils.toByteArray(entityStream));
                final Iterable<String> splitter = Splitter.on("\r\n").split(b64);
                for (final String split : splitter)
                {
                    builder.text(split);
                    builder.text("\n");
                }
                builder.endDocument();
            }
            return ImmutableList.of(builder.getNode());
        }
        return null;
    }

    private Iterable<XdmNode> constructMultipart(final HttpEntity entity, final ContentType contentMimeType, final String contentType) throws IOException
    {
        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.MULTIPART);
        final String boundary = contentMimeType.getParameter("boundary");
        builder.attribute(XProcXmlModel.Attributes.BOUNDARY, boundary);
        builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, contentType);
        final String content = EntityUtils.toString(entity);
        final Iterable<String> splitter = Splitter.on(boundary).split(content);
        for (final String split : splitter)       
        {
            if (!"--".equals(split))
            {
                final Iterable<Header> headers = extractHeaders(split);
                builder.nodes(constructBody(new ByteArrayInputStream(split.getBytes()), contentMimeType, contentType));
            }
        }
        builder.endDocument();
        return ImmutableList.of(builder.getNode());
     }

    private Iterable<Header> extractHeaders(final String content)
    {
        final Iterable<String> splitter = Splitter.on("\r\n").split(content);
        for (final String split : splitter)
        {
            BasicHeaderValueParser.parseElements(split, null);
        }
        return null;
    }

    private static String constructContentType(final Header contentType)
    {
        final StringBuilder builder = new StringBuilder();
        final HeaderElement elements = contentType.getElements()[0];
        builder.append(elements.getName());
        final NameValuePair[] parameters = elements.getParameters();
        for (final NameValuePair parameter : parameters)
        {
            builder.append("; ").append(parameter.getName()).append("=\"").append(parameter.getValue()).append("\"");
        }
        return builder.toString();
    }
}
