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
import net.sf.saxon.s9api.QName;
import org.trancecode.io.MediaTypes;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * User: Emmanuel Tourdot
 * Date: 12 febr. 2011
 * Time: 21:41:42
 */
public final class StepUtils
{
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String METHOD_XML = "xml";
    public static final String METHOD_HTML = "html";
    public static final String METHOD_XHTML = "xhtml";
    public static final String METHOD_TEXT = "text";

    private static final ImmutableMap<String, String> MEDIATYPES = ImmutableMap.of(METHOD_XML, MediaTypes.MEDIA_TYPE_XML,
        METHOD_HTML, MediaTypes.MEDIA_TYPE_HTML, METHOD_XHTML, MediaTypes.MEDIA_TYPE_XHTML, METHOD_TEXT, MediaTypes.MEDIA_TYPE_TEXT);

    private StepUtils()
    {
        // No instantiation
    }

    public static ImmutableMap<String, Object> getSerializationOptions(final AbstractStepProcessor.StepInput input,
        final ImmutableMap<QName, String> defaultOptions)
    {
        final ImmutableMap.Builder builder = new ImmutableMap.Builder<String, String>();

        final String encoding = input.getOptionValue(XProcOptions.ENCODING, DEFAULT_ENCODING);
        final Boolean byteOrderMark = "UTF-16".equals(encoding) ? Boolean.valueOf(input.getOptionValue(XProcOptions.BYTE_ORDER_MARK)) : false;
        builder.put(XProcOptions.BYTE_ORDER_MARK, byteOrderMark);

        final String cDataSection = input.getOptionValue(XProcOptions.CDATA_SECTION_ELEMENTS, defaultOptions.get(XProcOptions.CDATA_SECTION_ELEMENTS.getLocalName()));
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

        final QName method = new QName(input.getOptionValue(XProcOptions.METHOD, defaultOptions.get(XProcOptions.METHOD.getLocalName())));
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
            builder.put(XProcOptions.MEDIA_TYPE , mediaType);
        }

        putInBuilder(builder, input, defaultOptions, XProcOptions.NORMALIZATION_FORM, false);
        putInBuilder(builder, input, defaultOptions, XProcOptions.OMIT_XML_DECLARATION, true);
        putInBuilder(builder, input, defaultOptions, XProcOptions.STANDALONE, false);
        putInBuilder(builder, input, defaultOptions, XProcOptions.UNDECLARE_PREFIXES, true);
        putInBuilder(builder, input, defaultOptions, XProcOptions.VERSION, false);

        return builder.build();
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
            final Step step)
    {
        if (newPrefix != null)
        {
            if (newNamespace == null)
            {
                throw XProcExceptions.xd0034(step.getLocation());
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
                return new QName(newName, step.getNode());
            }
        }
        if (newName.contains(":"))
        {
            throw XProcExceptions.xd0034(step.getLocation());
        }
        else
        {
            return new QName("", newNamespace, newName);
        }
    }
}
