/*
 * Copyright (C) 2008 TranceCode Software
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
package org.trancecode.xproc;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import net.sf.saxon.s9api.QName;

/**
 * Common XProc options.
 * 
 * @author Herve Quiroz
 */
public final class XProcOptions
{
    public static final Map<QName, Object> EMPTY_OPTIONS_MAP = ImmutableMap.of();

    public static final QName ATTRIBUTE_NAME = new QName("attribute-name");
    public static final QName ATTRIBUTE_PREFIX = new QName("attribute-prefix");
    public static final QName ATTRIBUTE_NAMESPACE = new QName("attribute-namespace");
    public static final QName ATTRIBUTE_VALUE = new QName("attribute-value");
    public static final QName BYTE_ORDER_MARK = new QName("byte-order-mark");
    public static final QName CDATA_SECTION_ELEMENTS = new QName("cdata-section-elements");
    public static final QName CONTENT_TYPE = new QName("content-type");
    public static final QName DOCTYPE_PUBLIC = new QName("doctype-public");
    public static final QName DOCTYPE_SYSTEM = new QName("doctype-system");
    public static final QName DTD_VALIDATE = new QName("dtd-validate");
    public static final QName ENCODING = new QName("encoding");
    public static final QName ESCAPE_URI_ATTRIBUTES = new QName("escape-uri-attributes");
    public static final QName GROUP_ADJACENT = new QName("group-adjacent");
    public static final QName HREF = new QName("href");
    public static final QName INCLUDE_CONTENT_TYPE = new QName("include-content-type");
    public static final QName INDENT = new QName("indent");
    public static final QName INITIAL_MODE = new QName("initial-mode");
    public static final QName LIMIT = new QName("limit");
    public static final QName MATCH = new QName("match");
    public static final QName MEDIA_TYPE = new QName("media-type");
    public static final QName METHOD = new QName("method");
    public static final QName NORMALIZATION_FORM = new QName("normalization-form");
    public static final QName OMIT_XML_DECLARATION = new QName("omit-xml-declaration");
    public static final QName OUTPUT_BASE_URI = new QName("output-base-uri");
    public static final QName RESULT_BASE_URI = new QName("result-base-uri");
    public static final QName RESULT_DOCUMENT_URI = new QName("result-document-uri");
    public static final QName SOURCE_BASE_URI = new QName("source-base-uri");
    public static final QName SOURCE_DOCUMENT_URI = new QName("source-document-uri");
    public static final QName STANDALONE = new QName("standalone");
    public static final QName TEMP_BASE_URI = new QName("temp-base-uri");
    public static final QName TEMPLATE_NAME = new QName("template-name");
    public static final QName TEST = new QName("test");
    public static final QName UNDECLARE_PREFIXES = new QName("undeclare-prefixes");
    public static final QName VALIDATE = new QName("validate");
    public static final QName VERSION = new QName("version");
    public static final QName WRAPPER = new QName("wrapper");
    public static final QName WRAPPER_PREFIX = new QName("wrapper-prefix");
    public static final QName WRAPPER_NAMESPACE = new QName("wrapper-namespace");

    private XProcOptions()
    {
        // No instantiation
    }
}
