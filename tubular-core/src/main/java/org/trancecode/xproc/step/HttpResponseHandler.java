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
import java.io.IOException;
import java.util.List;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.stream.StreamSource;
import net.iharder.Base64;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.trancecode.api.Nullable;
import org.trancecode.http.BodypartResponseParser;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;

class HttpResponseHandler implements ResponseHandler<XProcHttpResponse>
{
    private final boolean detailed;
    private final boolean statusOnly;
    private final Processor processor;
    private final String overrideContentType;

    public HttpResponseHandler(final Processor processor, final boolean detailed, final boolean statusOnly,
                               @Nullable final String overrideContentType)
    {
        this.processor = processor;
        this.detailed = detailed;
        this.statusOnly = statusOnly;
        this.overrideContentType = overrideContentType;
    }

    @Override
    public XProcHttpResponse handleResponse(final HttpResponse httpResponse) throws IOException
    {
        final XProcHttpResponse response = new XProcHttpResponse();
        final HttpEntity entity = httpResponse.getEntity();
        final String contentCharset = EntityUtils.getContentCharSet(entity) == null ? "utf-8" : EntityUtils
                .getContentCharSet(entity);
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
        if (!StringUtils.isEmpty(overrideContentType))
        {
            contentMimeType = verifyContentType(contentMimeType, overrideContentType);
        }
        final BodypartResponseParser parser = new BodypartResponseParser(entity.getContent(), null,
                httpResponse.getParams(), contentType, contentCharset);

        if (!detailed)
        {
            if (!statusOnly)
            {
                if ("multipart".equals(contentMimeType.getPrimaryType()))
                {
                    response.setNodes(constructMultipart(contentMimeType, contentType, parser));
                }
                else
                {
                    final BodypartResponseParser.BodypartEntity part = parser.parseBodypart(false);
                    final Iterable<XdmNode> body = constructBody(contentMimeType, contentType, part);
                    if (body != null)
                    {
                        response.setNodes(body);
                    }
                }
            }
        }
        else
        {
            final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
            builder.startDocument();
            builder.startElement(XProcXmlModel.Elements.RESPONSE);
            builder.attribute(XProcXmlModel.Attributes.STATUS,
                    Integer.toString(httpResponse.getStatusLine().getStatusCode()));
            if (!statusOnly)
            {
                final Header[] headers = httpResponse.getAllHeaders();
                for (final Header header : headers)
                {
                    builder.startElement(XProcXmlModel.Elements.HEADER);
                    builder.attribute(XProcXmlModel.Attributes.NAME, header.getName());
                    builder.attribute(XProcXmlModel.Attributes.VALUE, header.getValue());
                    builder.endElement();
                }
                if ("multipart".equals(contentMimeType.getPrimaryType()))
                {
                    builder.nodes(constructMultipart(contentMimeType, contentType, parser));
                }
                else
                {
                    final BodypartResponseParser.BodypartEntity part = parser.parseBodypart(false);
                    final Iterable<XdmNode> body = constructBody(contentMimeType, contentType, part);
                    if (body != null)
                    {
                        builder.nodes(body);
                    }
                }
            }
            builder.endElement();
            builder.endDocument();
            response.setNodes(ImmutableList.of(builder.getNode()));
        }
        EntityUtils.consume(entity);
        return response;
    }

    private ContentType verifyContentType(final ContentType contentMimeType, final String overrideContentType)
    {
        ContentType overrideMimeType = null;
        try
        {
            overrideMimeType = new ContentType(overrideContentType);
        }
        catch (ParseException e)
        {
            throw XProcExceptions.xc0030();
        }
        if (StringUtils.equalsIgnoreCase(overrideMimeType.toString(),contentMimeType.toString()))
        {
            return contentMimeType;
        }
        if (("multipart".equals(contentMimeType.getPrimaryType()) && !"multipart".equals(overrideMimeType.getPrimaryType())) ||
            ("multipart".equals(overrideMimeType.getPrimaryType()) && !"multipart".equals(contentMimeType.getPrimaryType())) ||
            (contentMimeType.getSubType().contains("xml") || "text".equals(contentMimeType.getPrimaryType()) &&
                "image".equals(overrideMimeType.getPrimaryType())) ||
            ("image".equals(contentMimeType.getPrimaryType()) && (overrideMimeType.getSubType().contains("xml") ||
                "text".equals(overrideMimeType.getPrimaryType()))))
        {
            throw XProcExceptions.xc0030();
        }
        return overrideMimeType;
    }

    private Iterable<XdmNode> constructBody(final ContentType contentMimeType, final String contentType,
            final BodypartResponseParser.BodypartEntity part) throws IOException
    {
        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.BODY);
        builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, contentType);
        if (contentMimeType.getSubType().contains("xml"))
        {
            try
            {
                final XdmNode node = processor.newDocumentBuilder().build(
                        new StreamSource(part.getEntity().getContent()));
                if (!detailed)
                {
                    return ImmutableList.of(node);
                }
                else
                {
                    builder.nodes(node);
                }
            }
            catch (SaxonApiException sae)
            {
                return null;
            }
        }
        else
        {
            if ("text".equals(contentMimeType.getPrimaryType()))
            {
                builder.startContent();
                builder.text(IOUtils.toString(part.getEntity().getContent()));
            }
            else
            {
                builder.attribute(XProcXmlModel.Attributes.ENCODING, Steps.ENCODING_BASE64);
                builder.startContent();
                final String b64 = Base64.encodeBytes(IOUtils.toByteArray(part.getEntity().getContent()), Base64.DO_BREAK_LINES);
                final Iterable<String> splitter = Splitter.on("\r\n").split(b64);
                for (final String split : splitter)
                {
                    builder.text(split);
                    builder.text("\n");
                }
            }
        }
        builder.endDocument();
        return ImmutableList.of(builder.getNode());
    }

    private Iterable<XdmNode> constructMultipart(final ContentType contentMimeType,
            final String contentType, final BodypartResponseParser parser) throws IOException
    {
        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.MULTIPART);
        final String boundary = contentMimeType.getParameter("boundary");
        builder.attribute(XProcXmlModel.Attributes.BOUNDARY, boundary);
        parser.setBoundary(boundary);
        builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, contentType);
        final List<BodypartResponseParser.BodypartEntity> parts = parser.parseMultipart();
        for (final BodypartResponseParser.BodypartEntity part : parts)
        {
            final String mimeType = part.getHeaderGroup().getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
            final ContentType bodyCt = Steps.getContentType(mimeType, null);
            if (bodyCt.getSubType().contains("xml"))
            {
                builder.startElement(XProcXmlModel.Elements.BODY);
                builder.attribute(XProcXmlModel.Attributes.CONTENT_TYPE, mimeType);
            }
            final Iterable<XdmNode> body = constructBody(bodyCt, mimeType, part);
            if (body != null)
            {
                builder.nodes(body);
            }
            if (bodyCt.getSubType().contains("xml"))
            {
                builder.endElement();
            }
        }
        builder.endDocument();
        return ImmutableList.of(builder.getNode());
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
