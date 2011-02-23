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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;

/**
 * User: Emmanuel Tourdot
 * Date: 18 feb. 2011
 * Time: 06:35:39
 */
public class RequestParser
{
    private static final Logger LOG = Logger.getLogger(RequestParser.class);
    private static final String DEFAULT_MULTIPART_TYPE = "multipart/mixed";
    private final ImmutableMap<String, Object> serializationOptions;
    private final XProcHttpRequest request = new XProcHttpRequest();

    public RequestParser(final ImmutableMap<String, Object> serializationOptions)
    {
        this.serializationOptions = serializationOptions;
    }

    public XProcHttpRequest parseRequest(final XdmNode requestNode)
    {
        final String method = requestNode.getAttributeValue(XProcXmlModel.Attributes.METHOD);
        if (Strings.isNullOrEmpty(method))
        {
            throw XProcExceptions.xc0006(requestNode);
        }
        request.setHeaders(parseHeaders(requestNode));
        request.setEntity(parseMultipart(requestNode));
        if (!request.hasEntity())
        {
            request.setEntity(parseBody(requestNode));
        }
        if (request.hasEntity() &&
           !(StringUtils.equalsIgnoreCase(HttpPut.METHOD_NAME, method) || StringUtils.equalsIgnoreCase(HttpPost.METHOD_NAME, method)))
        {
            throw XProcExceptions.xc0005(requestNode);            
        }

        final boolean status = Boolean.valueOf(requestNode.getAttributeValue(XProcXmlModel.Attributes.STATUS_ONLY));
        final boolean detailed = Boolean.valueOf(requestNode.getAttributeValue(XProcXmlModel.Attributes.DETAILED));
        if (status && !detailed)
        {
            throw XProcExceptions.xc0004(requestNode);
        }
        request.setDetailled(detailed);
        request.setStatusOnly(status);
        
        final String href = requestNode.getAttributeValue(XProcXmlModel.Attributes.HREF);
        final URI hrefUri = requestNode.getBaseURI().resolve(href);
        if (hrefUri.getPort() != -1)
        {
            request.setHttpHost(new HttpHost(hrefUri.getHost(), hrefUri.getPort(), hrefUri.getScheme()));
        }
        else
        {
            request.setHttpHost(new HttpHost(hrefUri.getHost()));
        }

        final CredentialsProvider credentialsProvider = parseAuthentication(requestNode);
        request.setCredentials(credentialsProvider);
        request.setHttpRequest(constructMethod(method, hrefUri));

        return request;
    }

    private void checkCoherenceHeaders(final ImmutableList<Header> headers, final HttpEntity entity)
    {
    }

    private List<Header> parseHeaders(final XdmNode requestNode)
    {
        final List<Header> list = Lists.newArrayList();
        final Iterable<XdmNode> childs = SaxonAxis.childElements(requestNode, XProcXmlModel.Elements.HEADER);
        for (final XdmNode child : childs)
        {
            final String nameHeader = child.getAttributeValue(XProcXmlModel.Attributes.NAME);
            final String valueHeader = child.getAttributeValue(XProcXmlModel.Attributes.VALUE);
            if (Strings.isNullOrEmpty(nameHeader) && Strings.isNullOrEmpty(valueHeader))
            {
                list.add(new BasicHeader(nameHeader, valueHeader));
            }
        }
        return list;
    }

    private MultipartEntity parseMultipart(final XdmNode requestNode)
    {
        final XdmNode child = SaxonAxis.childElement(requestNode, XProcXmlModel.Elements.MULTIPART);
        if (child != null)
        {
            final String contentTypeAtt = child.getAttributeValue(XProcXmlModel.Attributes.CONTENT_TYPE);
            final String contentType = Strings.isNullOrEmpty(contentTypeAtt) ? DEFAULT_MULTIPART_TYPE : contentTypeAtt;
            final String boundary = child.getAttributeValue(XProcXmlModel.Attributes.BOUNDARY);
            if (StringUtils.startsWith(boundary, "--"))
            {
                throw XProcExceptions.xc0002(requestNode);
            }
            final MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT, boundary, Charset.forName("UTF-8"))
            {
                @Override
                protected String generateContentType(final String boundary, final Charset charset)
                {
                    final StringBuilder buffer = new StringBuilder();
                    buffer.append(contentType).append("; boundary=").append(boundary);
                    return buffer.toString();
                }
            };
            final Iterable<XdmNode> bodies = SaxonAxis.childElements(child, XProcXmlModel.Elements.BODY);
            for (final XdmNode body : bodies)
            {
                final StringEntity contentBody = getContentBody(body);
                if (contentBody != null)
                {
                    try
                    {
                        reqEntity.addPart("part", new StringBody(IOUtils.toString(contentBody.getContent())));
                    }
                    catch (IOException e)
                    {
                        return null;
                    }
                }
            }

            return reqEntity;
        }
        return null;
    }

    private StringEntity getContentBody(final XdmNode node)
    {
        final String contentTypeAtt = node.getAttributeValue(XProcXmlModel.Attributes.CONTENT_TYPE);
        final String encoding = node.getAttributeValue(XProcXmlModel.Attributes.ENCODING);
        final String id = node.getAttributeValue(XProcXmlModel.Attributes.ID);
        if (id != null)
        {
            request.getHeaders().add(new BasicHeader("Content-ID", id));
        }
        final String description = node.getAttributeValue(XProcXmlModel.Attributes.DESCRIPTION);
        if (description != null)
        {
            request.getHeaders().add(new BasicHeader("Content-Description", description));
        }
        final String disposition = node.getAttributeValue(XProcXmlModel.Attributes.DISPOSITION);
        if (disposition != null)
        {
            request.getHeaders().add(new BasicHeader("Content-Disposition", disposition));
        }

        try
        {
            final ContentType contentType = new ContentType(contentTypeAtt);
            final String charset = contentType.getParameter("charset");
            final StringBuilder contentBuilder = new StringBuilder();
            if (!StringUtils.equalsIgnoreCase("xml",contentType.getSubType()) || StringUtils.equalsIgnoreCase("base64", encoding))
            {
                final Iterable<XdmItem> childs = SaxonAxis.axis(node, Axis.CHILD);
                for (final XdmItem aNode : childs)
                {
                    if (!XdmNodeKind.TEXT.equals(((XdmNode) aNode).getNodeKind()))
                    {
                        throw XProcExceptions.xc0022(node);                        
                    }
                    else
                    {
                        contentBuilder.append(aNode.toString());
                    }
                }
            }
            if (StringUtils.equalsIgnoreCase("xml",contentType.getSubType()))
            {
                final ByteArrayOutputStream targetOutputStream = new ByteArrayOutputStream();
                final Serializer serializer = StepUtils.getSerializer(targetOutputStream, serializationOptions);
                serializer.setOutputProperty(Serializer.Property.MEDIA_TYPE, contentType.toString());
                try
                {
                    node.getProcessor().writeXdmValue(SaxonAxis.childElement(node), serializer);
                }
                catch (final Exception e)
                {
                    throw new PipelineException("Error while trying to write document", e);
                }
                finally
                {
                    Closeables.closeQuietly(targetOutputStream);
                }
                contentBuilder.append(targetOutputStream.toString());
            }
            if (charset != null)
            {
                return new StringEntity(contentBuilder.toString(), contentType.toString(), charset);
            }
            else
            {
                return new StringEntity(contentBuilder.toString(), contentType.toString(), "utf-8");
            }

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private StringEntity parseBody(final XdmNode requestNode)
    {
        final XdmNode body = SaxonAxis.childElement(requestNode, XProcXmlModel.Elements.BODY);
        if (body != null)
        {
            return getContentBody(body);
        }
        return null;
    }

    private CredentialsProvider parseAuthentication(final XdmNode requestNode)
    {
        final String username = requestNode.getAttributeValue(XProcXmlModel.Attributes.USERNAME);
        if (!Strings.isNullOrEmpty(username))
        {
            final String password = requestNode.getAttributeValue(XProcXmlModel.Attributes.PASSWORD);
            final String authMethod = requestNode.getAttributeValue(XProcXmlModel.Attributes.AUTH_METHOD);
            if (!StringUtils.equalsIgnoreCase(AuthPolicy.BASIC,authMethod) &&
                !StringUtils.equalsIgnoreCase(AuthPolicy.DIGEST,authMethod))
            {
                throw XProcExceptions.xc0003(requestNode);
            }

            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            final HttpHost httpHost = request.getHttpHost();
            credsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                    new UsernamePasswordCredentials(username, password));

            return credsProvider;
        }
        return null;
    }

    private HttpContext parseContext(final AuthPolicy policy)
    {
        final HttpHost httpHost = request.getHttpHost();
        final AuthCache authCache = new BasicAuthCache();
        if (AuthPolicy.BASIC.equalsIgnoreCase(policy.toString()))
        {
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(httpHost, basicAuth);
        }
        else
        {
            final DigestScheme digestAuth = new DigestScheme();
            authCache.put(httpHost, digestAuth);
        }
        final BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        return localContext;
    }

    private HttpRequestBase constructMethod(final String method, final URI hrefUri)
    {
        final HttpEntity httpEntity = request.getEntity();
        final List<Header> headers = request.getHeaders();
        if (StringUtils.equalsIgnoreCase(HttpPost.METHOD_NAME, method))
        {
            final HttpPost httpPost = new HttpPost(hrefUri);
            httpPost.setEntity(httpEntity);
            return httpPost;
        }
        else if (StringUtils.equalsIgnoreCase(HttpPut.METHOD_NAME, method))
        {
        }
        else if (StringUtils.equalsIgnoreCase(HttpDelete.METHOD_NAME, method))
        {
        }
        else if (StringUtils.equalsIgnoreCase(HttpGet.METHOD_NAME, method))
        {
            final HttpGet httpGet = new HttpGet(hrefUri);
            if (headers != null && headers.size() > 0)
            {
                httpGet.setHeaders((Header[]) headers.toArray());
            }
            return httpGet;

        }
        else if (StringUtils.equalsIgnoreCase(HttpHead.METHOD_NAME, method))
        {
        }
        else if (StringUtils.equalsIgnoreCase(HttpPost.METHOD_NAME, method))
        {
        }
        return null;
    }
}
