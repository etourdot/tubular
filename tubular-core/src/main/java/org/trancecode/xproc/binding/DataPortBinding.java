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
package org.trancecode.xproc.binding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import javax.mail.internet.ContentType;
import net.iharder.Base64;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.trancecode.api.Immutable;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.step.Steps;

/**
 * @author Emmanuel Tourdot
 */
@Immutable
public class DataPortBinding extends AbstractPortBinding
{
    private final String href;
    private final QName wrapper;
    private final XdmNode node;
    private final ContentType contentType;

    public DataPortBinding(final String href, final String wrapperName, final String wrapperPrefix,
                           final String wrapperNamespace, final String contentType, final XdmNode node)
    {
        super(SaxonLocation.of(node));
        this.href = Preconditions.checkNotNull(href);
        if (Strings.isNullOrEmpty(wrapperName))
        {
            this.wrapper = null;
        }
        else
        {
            this.wrapper = Steps.getNewNamespace(wrapperPrefix, wrapperNamespace, wrapperName,
                    this.getLocation(), node);
        }
        this.node = node;
        if (Strings.isNullOrEmpty(contentType))
        {
            this.contentType = null;
        }
        else
        {
            this.contentType = Steps.getContentType(contentType, node);
        }
    }

    @Override
    public EnvironmentPortBinding newEnvironmentPortBinding(final Environment environment)
    {
        return new AbstractEnvironmentPortBinding(location)
        {
            public Iterable<XdmNode> readNodes()
            {
                final SaxonBuilder builder = new SaxonBuilder(environment.getPipelineContext().getProcessor()
                    .getUnderlyingConfiguration());
                builder.startDocument();
                if (wrapper != null)
                {
                    builder.startElement(wrapper);
                }
                else
                {
                    builder.startElement(XProcXmlModel.Elements.C_DATA);
                }
                writeContent(builder);
                builder.endElement();
                builder.endDocument();
                return ImmutableList.of(builder.getNode());
            }
        };
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s]", getClass().getSimpleName(), href);
    }

    private void writeContent(final SaxonBuilder builder)
    {
        final URI uri = URI.create(href);
        if (uri.getScheme()!=null && !StringUtils.equals("file",uri.getScheme()) &&
            !StringUtils.equals("http",uri.getScheme()))
        {
            throw XProcExceptions.xd0012(this.getLocation(), uri.toASCIIString());
        }
        try
        {
            final URL url;
            if (uri.isAbsolute())
            {
                url = uri.toURL();
            }
            else
            {
                url = node.getBaseURI().resolve(uri).toURL();
            }
            final QName contentTypeAtt = (wrapper == null)? XProcXmlModel.Attributes.CONTENT_TYPE :
                                                            XProcXmlModel.Attributes.C_CONTENT_TYPE;
            final QName encodingAtt = (wrapper == null)? XProcXmlModel.Attributes.ENCODING :
                                                            XProcXmlModel.Attributes.C_ENCODING;
            final URLConnection urlConnection = url.openConnection();
            final ContentType guessContentType;
            if (StringUtils.equals("http", url.getProtocol()))
            {
                guessContentType = Steps.getContentType(urlConnection.getContentType(), node);
            }
            else
            {
                if (contentType != null)
                {
                    guessContentType = contentType;
                }
                else
                {
                    guessContentType = Steps.getContentType("application/octet-stream ; encoding="
                                                            + Steps.ENCODING_BASE64, node);
                }
            }
            final Charset charset;
            if (contentType != null && contentType.getParameter("charset") != null)
            {
                charset = Charset.forName(contentType.getParameter("charset"));
            }
            else
            {
                charset = Charset.forName("UTF-8");
            }

            final InputStream stream = urlConnection.getInputStream();
            builder.attribute(contentTypeAtt, Steps.contentTypeToString(guessContentType));
            if (StringUtils.equals("text",guessContentType.getPrimaryType()) ||
                StringUtils.contains(guessContentType.getSubType(), "xml"))
            {
                if (guessContentType.getParameter("encoding") != null)
                {
                    builder.attribute(encodingAtt, guessContentType.getParameter("encoding"));
                }
                builder.startContent();
                builder.text(IOUtils.toString(stream, charset.name()));
            }
            else
            {
                builder.attribute(encodingAtt, Steps.ENCODING_BASE64);
                builder.startContent();
                builder.text(Base64.encodeBytes(IOUtils.toByteArray(stream), Base64.DO_BREAK_LINES));
            }
        }
        catch (final MalformedURLException mue)
        {
            throw XProcExceptions.xd0029(this.getLocation());
        }
        catch (final IOException ioe)
        {
            throw XProcExceptions.xd0029(this.getLocation());
        }
    }
}
