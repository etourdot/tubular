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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.IOUtils;
import org.trancecode.io.MediaTypes;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xml.XmlException;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.PortReference;
import org.trancecode.xproc.step.Step.Log;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * User: Emmanuel Tourdot Date: 12 febr. 2011 Time: 21:41:42
 */
public final class StepUtils
{
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String METHOD_XML = "xml";
    public static final String METHOD_HTML = "html";
    public static final String METHOD_XHTML = "xhtml";
    public static final String METHOD_TEXT = "text";
    public static final String ENCODING_BASE64 = "base64";
    public static final Set<String> SUPPORTED_CONTENTTYPE = ImmutableSet.of(MediaTypes.MEDIA_XML,
            MediaTypes.MEDIA_TYPE_HTML);
    private static final ImmutableMap<String, String> MEDIATYPES = ImmutableMap.of(METHOD_XML,
            MediaTypes.MEDIA_TYPE_XML, METHOD_HTML, MediaTypes.MEDIA_TYPE_HTML, METHOD_XHTML,
            MediaTypes.MEDIA_TYPE_XHTML, METHOD_TEXT, MediaTypes.MEDIA_TYPE_TEXT);
    private static Logger LOG = Logger.getLogger(StepUtils.class);

    private StepUtils()
    {
        // No instantiation
    }

    public static ImmutableMap<String, Object> getSerializationOptions(final AbstractStepProcessor.StepInput input,
            final ImmutableMap<QName, String> defaultOptions)
    {
        final ImmutableMap.Builder builder = new ImmutableMap.Builder<String, String>();

        if (input.getStep().hasOptionDeclared(XProcOptions.ENCODING))
        {
            final String encoding = input.getOptionValue(XProcOptions.ENCODING, DEFAULT_ENCODING);
            builder.put(XProcOptions.ENCODING, encoding);
            final Boolean byteOrderMark = "UTF-16".equals(encoding) ? Boolean.valueOf(input
                    .getOptionValue(XProcOptions.BYTE_ORDER_MARK)) : false;
            builder.put(XProcOptions.BYTE_ORDER_MARK, byteOrderMark);
        }

        if (input.getStep().hasOptionDeclared(XProcOptions.CDATA_SECTION_ELEMENTS))
        {
            final String cDataSection = input.getOptionValue(XProcOptions.CDATA_SECTION_ELEMENTS,
                    defaultOptions.get(XProcOptions.CDATA_SECTION_ELEMENTS.getLocalName()));
            if (cDataSection != null)
            {
                final Iterable<String> sections = Splitter.on(" ").omitEmptyStrings().split(cDataSection);
                final ImmutableList.Builder<QName> cDataBuilder = new ImmutableList.Builder<QName>();
                for (final String section : sections)
                {
                    cDataBuilder.add(new QName(section));
                }
                builder.put(XProcOptions.CDATA_SECTION_ELEMENTS, cDataBuilder.build());
            }
        }

        final QName method = new QName(input.getOptionValue(XProcOptions.METHOD,
                defaultOptions.get(XProcOptions.METHOD.getLocalName())));
        builder.put(XProcOptions.METHOD, method);

        putInBuilder(builder, input, defaultOptions, XProcOptions.DOCTYPE_PUBLIC, false);
        putInBuilder(builder, input, defaultOptions, XProcOptions.DOCTYPE_SYSTEM, false);

        if (METHOD_XML.equals(method.getLocalName()) || METHOD_HTML.equals(method.getLocalName()))
        {
            putInBuilder(builder, input, defaultOptions, XProcOptions.ESCAPE_URI_ATTRIBUTES, true);
            putInBuilder(builder, input, defaultOptions, XProcOptions.INCLUDE_CONTENT_TYPE, true);
        }
        putInBuilder(builder, input, defaultOptions, XProcOptions.INDENT, true);

        final String mediaType = input.getOptionValue(XProcOptions.MEDIA_TYPE, null);
        if (mediaType == null)
        {
            builder.put(XProcOptions.MEDIA_TYPE, MEDIATYPES.get(method.getLocalName()));
        }
        else
        {
            builder.put(XProcOptions.MEDIA_TYPE, mediaType);
        }

        if (input.getStep().hasOptionDeclared(XProcOptions.NORMALIZATION_FORM))
        {
            putInBuilder(builder, input, defaultOptions, XProcOptions.NORMALIZATION_FORM, false);
        }
        putInBuilder(builder, input, defaultOptions, XProcOptions.OMIT_XML_DECLARATION, true);
        putInBuilder(builder, input, defaultOptions, XProcOptions.STANDALONE, false);
        putInBuilder(builder, input, defaultOptions, XProcOptions.UNDECLARE_PREFIXES, true);
        putInBuilder(builder, input, defaultOptions, XProcOptions.VERSION, false);

        return builder.build();
    }

    public static Serializer getSerializer(final OutputStream stream, final ImmutableMap<String, Object> options)
    {
        final Serializer serializer = new Serializer();
        serializer.setOutputStream(stream);
        if (options.containsKey(XProcOptions.DOCTYPE_PUBLIC))
        {
            serializer.setOutputProperty(Serializer.Property.DOCTYPE_PUBLIC, options.get(XProcOptions.DOCTYPE_PUBLIC)
                    .toString());
        }
        if (options.containsKey(XProcOptions.DOCTYPE_SYSTEM))
        {
            serializer.setOutputProperty(Serializer.Property.DOCTYPE_SYSTEM, options.get(XProcOptions.DOCTYPE_PUBLIC)
                    .toString());
        }
        serializer.setOutputProperty(Serializer.Property.METHOD,
                ((QName) options.get(XProcOptions.METHOD)).getLocalName());
        serializer.setOutputProperty(Serializer.Property.ESCAPE_URI_ATTRIBUTES,
                (Boolean) options.get(XProcOptions.ESCAPE_URI_ATTRIBUTES) ? "yes" : "no");
        serializer.setOutputProperty(Serializer.Property.MEDIA_TYPE, options.get(XProcOptions.MEDIA_TYPE).toString());
        serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,
                (Boolean) options.get(XProcOptions.OMIT_XML_DECLARATION) ? "yes" : "no");
        serializer.setOutputProperty(Serializer.Property.INDENT, (Boolean) options.get(XProcOptions.INDENT) ? "yes"
                : "no");
        serializer.setOutputProperty(Serializer.Property.INCLUDE_CONTENT_TYPE,
                (Boolean) options.get(XProcOptions.INCLUDE_CONTENT_TYPE) ? "yes" : "no");
        return serializer;
    }

    private static void putInBuilder(final ImmutableMap.Builder builder, final AbstractStepProcessor.StepInput input,
            final ImmutableMap<QName, String> defaultOptions, final QName option, final boolean isBoolean)
    {
        if (input.getOptionValue(option) == null)
        {
            return;
        }
        final String defOption = defaultOptions.get(option.getLocalName());
        if (defOption != null)
        {
            if (isBoolean)
            {
                builder.put(option, Boolean.valueOf(input.getOptionValue(option, defOption)));
            }
            else
            {
                builder.put(option, input.getOptionValue(option, defOption));
            }
        }
        else
        {
            if (isBoolean)
            {
                builder.put(option, Boolean.valueOf(input.getOptionValue(option)));
            }
            else
            {
                builder.put(option, input.getOptionValue(option));
            }
        }
    }

    public static QName getNewNamespace(final String newPrefix, final String newNamespace, final String newName,
            final Location location, final XdmNode node)
    {
        if (newPrefix != null)
        {
            if (newNamespace == null)
            {
                throw XProcExceptions.xd0034(location);
            }
            else
            {
                return new QName(newPrefix, newNamespace, newName);
            }
        }
        else
        {
            if (newNamespace == null)
            {
                return new QName(newName, node);
            }
        }
        if (newName.contains(":"))
        {
            throw XProcExceptions.xd0034(location);
        }
        else
        {
            final String prefix = (newPrefix == null) ? node.getProcessor().getUnderlyingConfiguration().getNamePool()
                    .suggestPrefixForURI(newNamespace) : newPrefix;
            return new QName((prefix == null) ? "" : prefix, newNamespace, newName);
        }
    }

    public static String getBase64Content(final String content, final ContentType contentType, final String charset)
    {
        try
        {
            final InputStream b64is = MimeUtility.decode(new ByteArrayInputStream(content.getBytes(charset)),
                    ENCODING_BASE64);
            final StringWriter writer = new StringWriter();
            IOUtils.copy(b64is, writer, charset);
            return writer.toString();
        }
        catch (final MessagingException e)
        {
            throw XProcExceptions.xc0010(null);
        }
        catch (final UnsupportedEncodingException e)
        {
            throw XProcExceptions.xc0010(null);
        }
        catch (final IOException e)
        {
            throw XProcExceptions.xc0010(null);
        }
    }

    public static ContentType getContentType(final String mimeType, final XdmNode node)
    {
        final ContentType contentType;
        try
        {
            contentType = new ContentType(mimeType);
        }
        catch (final ParseException e)
        {
            throw XProcExceptions.xc0020(node);
        }
        return contentType;
    }

    public static URI getUri(final String namespace)
    {
        if (namespace == null)
        {
            return null;
        }
        try
        {
            final URI uri = new URI(namespace);
            if (!uri.isAbsolute())
            {
                return null;
            }
            else
            {
                return uri.resolve(namespace);
            }
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }

    public static void writeLogs(final Step step, final Environment environment)
    {
        LOG.trace("{@method} step = {}", step.getName());
        for (final Log log : step.getLogs())
        {
            LOG.trace("  write {}/{} to {}", step.getName(), log.getPort(), log.getHref());
            final EnvironmentPort port = environment.getEnvironmentPort(PortReference.newReference(step.getName(),
                    log.getPort()));
            for (final XdmNode node : port.readNodes())
            {
                final OutputURIResolver resolver = environment.getPipelineContext().getProcessor()
                        .getUnderlyingConfiguration().getOutputURIResolver();
                final Result result;
                try
                {
                    result = resolver.resolve(log.getHref(), environment.getBaseUri().toString());
                }
                catch (final TransformerException e)
                {
                    throw new XmlException(e, "cannot write node from port %s/%s to %s", step.getName(), log.getPort(),
                            log.getHref());
                }
                LOG.trace("  output URI = %s", result.getSystemId());

                try
                {
                    new TransformerFactoryImpl().newTransformer().transform(node.asSource(), result);
                }
                catch (final TransformerException e)
                {
                    throw new XmlException(e, "cannot write node from port %s/%s to %s", step.getName(), log.getPort(),
                            log.getHref());
                }
                finally
                {
                    try
                    {
                        resolver.close(result);
                    }
                    catch (final TransformerException e)
                    {
                        LOG.error("{}", e);
                        LOG.trace("{stackTrace}", e);
                    }
                }
            }
        }
    }
}
