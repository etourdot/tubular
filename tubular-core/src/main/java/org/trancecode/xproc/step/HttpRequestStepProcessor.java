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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Map;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.BasicHttpContext;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:http-request standard XProc step.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.http-request">p:http-request</a>
 */
@ExternalResources(read = true, write = true)
public final class HttpRequestStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(HttpRequestStepProcessor.class);
    private static final Map<QName, String> DEFAULT_SERIALIZATION_OPTIONS;

    static
    {
        final Builder<QName, String> builder = ImmutableMap.builder();
        builder.put(XProcOptions.CDATA_SECTION_ELEMENTS, "").put(XProcOptions.ESCAPE_URI_ATTRIBUTES, "false")
                .put(XProcOptions.INCLUDE_CONTENT_TYPE, "true").put(XProcOptions.INDENT, "false")
                .put(XProcOptions.METHOD, "xml").put(XProcOptions.NORMALIZATION_FORM, "none")
                .put(XProcOptions.OMIT_XML_DECLARATION, "true").put(XProcOptions.STANDALONE, "omit")
                .put(XProcOptions.VERSION, "1.0");
        DEFAULT_SERIALIZATION_OPTIONS = builder.build();
    }

    @Override
    public QName getStepType()
    {
        return XProcSteps.HTTP_REQUEST;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDoc = input.readNode(XProcPorts.SOURCE);
        final XdmNode request = SaxonAxis.childElement(sourceDoc);
        if (!XProcXmlModel.Elements.REQUEST.equals(request.getNodeName()))
        {
            throw XProcExceptions.xc0040(input.getStep().getLocation());
        }

        final Map<QName, Object> serializationOptions = Steps.getSerializationOptions(input,
                DEFAULT_SERIALIZATION_OPTIONS);
        LOG.trace("  serializationOptions = {}", serializationOptions);

        final RequestParser parser = new RequestParser(serializationOptions);
        final XProcHttpRequest xProcRequest = parser.parseRequest(request);
        final URI uri = xProcRequest.getHttpRequest().getURI();
        if (uri.getScheme()!=null && !StringUtils.equals("file",uri.getScheme()) &&
            !StringUtils.equals("http",uri.getScheme()))
        {
            throw XProcExceptions.xd0012(input.getLocation(), uri.toASCIIString());
        }

        final BasicHttpContext localContext = new BasicHttpContext();
        final HttpClient httpClient = prepareHttpClient(xProcRequest, localContext);
        try
        {
            final Processor processor = input.getPipelineContext().getProcessor();
            final ResponseHandler<XProcHttpResponse> responseHandler = new HttpResponseHandler(processor,
                    xProcRequest.isDetailled(), xProcRequest.isStatusOnly(), xProcRequest.getOverrideContentType());
            final XProcHttpResponse response = httpClient.execute(xProcRequest.getHttpRequest(), responseHandler,
                    localContext);
            final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
            builder.startDocument();
            if (response.getNodes() != null)
            {
                builder.nodes(response.getNodes());
            }
            builder.endDocument();
            output.writeNodes(XProcPorts.RESULT, builder.getNode());
        }
        catch (final IOException e)
        {
            // TODO
            e.printStackTrace();
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private HttpClient prepareHttpClient(final XProcHttpRequest xProcRequest, final BasicHttpContext localContext)
    {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        final ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(schemeRegistry);
        final DefaultHttpClient httpClient = new DefaultHttpClient(connManager);
        final ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(httpClient.getConnectionManager()
                .getSchemeRegistry(), ProxySelector.getDefault());
        httpClient.setRoutePlanner(routePlanner);

        if (xProcRequest.getCredentials() != null)
        {
            final List<String> authPref = Lists.newArrayList(AuthPolicy.BASIC, AuthPolicy.DIGEST);
            httpClient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authPref);
            httpClient.setCredentialsProvider(xProcRequest.getCredentials());
            final AuthCache authCache = new BasicAuthCache();
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(xProcRequest.getHttpHost(), basicAuth);
            localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        }
        return httpClient;
    }
}
