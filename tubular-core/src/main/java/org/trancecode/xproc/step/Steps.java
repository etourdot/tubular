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
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.iharder.Base64;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.io.MediaTypes;
import org.trancecode.lang.TcBooleans;
import org.trancecode.lang.TcStrings;
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
 * Utility methods related to steps.
 * 
 * @author Emmanuel Tourdot
 */
public final class Steps
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
    private static final Logger LOG = Logger.getLogger(Steps.class);

    private Steps()
    {
        // No instantiation
    }

    public static Map<QName, Object> getSerializationOptions(final AbstractStepProcessor.StepInput input,
            final Map<QName, String> defaultOptions)
    {
        final Builder<QName, Object> builder = ImmutableMap.builder();

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
                    defaultOptions.get(XProcOptions.CDATA_SECTION_ELEMENTS));
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

        if (input.getStep().hasOptionDeclared(XProcOptions.METHOD))
        {
            final QName method = new QName(input.getOptionValue(XProcOptions.METHOD,
                    defaultOptions.get(XProcOptions.METHOD)));
            builder.put(XProcOptions.METHOD, method);
            if (METHOD_XML.equals(method.getLocalName()) || METHOD_HTML.equals(method.getLocalName()))
            {
                putInBuilder(builder, input, defaultOptions, XProcOptions.ESCAPE_URI_ATTRIBUTES, true);
                putInBuilder(builder, input, defaultOptions, XProcOptions.INCLUDE_CONTENT_TYPE, true);
            }
            final String mediaType = input.getOptionValue(XProcOptions.MEDIA_TYPE, null);
            if (mediaType == null)
            {
                builder.put(XProcOptions.MEDIA_TYPE, MEDIATYPES.get(method.getLocalName()));
            }
            else
            {
                builder.put(XProcOptions.MEDIA_TYPE, mediaType);
            }
        }

        putInBuilder(builder, input, defaultOptions, XProcOptions.DOCTYPE_PUBLIC, false);
        putInBuilder(builder, input, defaultOptions, XProcOptions.DOCTYPE_SYSTEM, false);

        putInBuilder(builder, input, defaultOptions, XProcOptions.INDENT, true);

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

    public static Serializer getSerializer(final OutputStream stream, final Map<QName, Object> options)
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
        serializer.setOutputProperty(Serializer.Property.METHOD, TcStrings.toString(options.get(XProcOptions.METHOD)));
        serializer.setOutputProperty(Serializer.Property.ESCAPE_URI_ATTRIBUTES,
                TcBooleans.getValue((Boolean) options.get(XProcOptions.ESCAPE_URI_ATTRIBUTES)) ? "yes" : "no");
        serializer.setOutputProperty(Serializer.Property.MEDIA_TYPE,
                TcStrings.toString(options.get(XProcOptions.MEDIA_TYPE)));
        serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,
                TcBooleans.getValue((Boolean) options.get(XProcOptions.OMIT_XML_DECLARATION)) ? "yes" : "no");
        serializer.setOutputProperty(Serializer.Property.INDENT,
                TcBooleans.getValue((Boolean) options.get(XProcOptions.INDENT)) ? "yes" : "no");
        serializer.setOutputProperty(Serializer.Property.INCLUDE_CONTENT_TYPE,
                TcBooleans.getValue((Boolean) options.get(XProcOptions.INCLUDE_CONTENT_TYPE)) ? "yes" : "no");
        return serializer;
    }

    private static void putInBuilder(final Builder<QName, Object> builder, final AbstractStepProcessor.StepInput input,
            final Map<QName, String> defaultOptions, final QName option, final boolean isBoolean)
    {
        if (input.getOptionValue(option) == null)
        {
            return;
        }
        final String defaultOption = defaultOptions.get(option);
        if (defaultOption != null)
        {
            if (isBoolean)
            {
                builder.put(option, Boolean.valueOf(input.getOptionValue(option, defaultOption)));
            }
            else
            {
                builder.put(option, input.getOptionValue(option, defaultOption));
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

    public static String getBase64Content(final String content, final String charset)
    {
        try
        {
            return new String(Base64.decode(content.getBytes(charset)), charset);
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw XProcExceptions.xc0010(null);
        }
        catch (final IOException ioe)
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
        catch (final URISyntaxException e)
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
                    if (log.getHref() != null)
                    {
                        result = resolver.resolve(log.getHref(), environment.getBaseUri().toString());
                    }
                    else
                    {
                        result = new StreamResult(System.err);
                    }
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
                    if (log.getHref() != null)
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

    public static String contentTypeToString(final ContentType contentType)
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(contentType.getPrimaryType()).append("/").append(contentType.getSubType());
        if (contentType.getParameter("charset") != null)
        {
            buffer.append("; charset=\"").append(contentType.getParameter("charset").toLowerCase()).append("\"");
        }
        return buffer.toString();
    }

    public static Charset getCharset(final String charset)
    {
        return charset != null ? Charset.forName(charset) : Charset.forName("utf-8");
    }
}
