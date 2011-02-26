/*
 * Copyright (C) 2008 Herve Quiroz
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
package org.trancecode.xproc.variable;

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

    public static final QName ALL = new QName("all");
    public static final QName ARGS = new QName("args");
    public static final QName ARG_SEPARATOR = new QName("arg-separator");
    public static final QName ASSERT_VALID = new QName("assert-valid");
    public static final QName ATTRIBUTE = new QName("attribute");
    public static final QName ATTRIBUTE_NAME = new QName("attribute-name");
    public static final QName ATTRIBUTE_PREFIX = new QName("attribute-prefix");
    public static final QName ATTRIBUTE_NAMESPACE = new QName("attribute-namespace");
    public static final QName ATTRIBUTE_VALUE = new QName("attribute-value");
    public static final QName BASE_URI = new QName("base-uri");
    public static final QName BYTE_ORDER_MARK = new QName("byte-order-mark");
    public static final QName CDATA_SECTION_ELEMENTS = new QName("cdata-section-elements");
    public static final QName CHARSET = new QName("charset");
    public static final QName CODE = new QName("code");
    public static final QName CODE_PREFIX = new QName("code-prefix");
    public static final QName CODE_NAMESPACE = new QName("code-namespace");
    public static final QName COMMAND = new QName("command");
    public static final QName CONTENT_TYPE = new QName("content-type");
    public static final QName CWD = new QName("cwd");
    public static final QName DOCTYPE_PUBLIC = new QName("doctype-public");
    public static final QName DOCTYPE_SYSTEM = new QName("doctype-system");
    public static final QName DTD_VALIDATE = new QName("dtd-validate");
    public static final QName ENCODING = new QName("encoding");
    public static final QName ERRORS_IS_XML = new QName("errors-is-xml");
    public static final QName ESCAPE_URI_ATTRIBUTES = new QName("escape-uri-attributes");
    public static final QName FAIL_IF_NOT_EQUAL = new QName("fail-if-not-equal");
    public static final QName FAILURE_THRESHOLD = new QName("failure-threshold");
    public static final QName GROUP_ADJACENT = new QName("group-adjacent");
    public static final QName HREF = new QName("href");
    public static final QName INCLUDE_CONTENT_TYPE = new QName("include-content-type");
    public static final QName INDENT = new QName("indent");
    public static final QName INITIAL_MODE = new QName("initial-mode");
    public static final QName LABEL = new QName("label");
    public static final QName LIMIT = new QName("limit");
    public static final QName MATCH = new QName("match");
    public static final QName MEDIA_TYPE = new QName("media-type");
    public static final QName METHOD = new QName("method");
    public static final QName MODE = new QName("mode");
    public static final QName NAMESPACE = new QName("namespace");
    public static final QName NEW_NAME = new QName("new-name");
    public static final QName NEW_PREFIX = new QName("new-prefix");
    public static final QName NEW_NAMESPACE = new QName("new-namespace");
    public static final QName NORMALIZATION_FORM = new QName("normalization-form");
    public static final QName OMIT_XML_DECLARATION = new QName("omit-xml-declaration");
    public static final QName OUTPUT_BASE_URI = new QName("output-base-uri");
    public static final QName PATH_SEPARATOR = new QName("path-separator");
    public static final QName POSITION = new QName("position");
    public static final QName RELATIVE = new QName("relative");
    public static final QName REPLACE = new QName("replace");
    public static final QName RESULT_BASE_URI = new QName("result-base-uri");
    public static final QName RESULT_DOCUMENT_URI = new QName("result-document-uri");
    public static final QName RESULT_IS_XML = new QName("result-is-xml");
    public static final QName SELECT = new QName("select");
    public static final QName SOURCE_BASE_URI = new QName("source-base-uri");
    public static final QName SOURCE_DOCUMENT_URI = new QName("source-document-uri");
    public static final QName SOURCE_IS_XML = new QName("source-is-xml");
    public static final QName STANDALONE = new QName("standalone");
    public static final QName TEMP_BASE_URI = new QName("temp-base-uri");
    public static final QName TEMPLATE_NAME = new QName("template-name");
    public static final QName TEST = new QName("test");
    public static final QName TRY_NAMESPACES = new QName("try-namespaces");
    public static final QName UNDECLARE_PREFIXES = new QName("undeclare-prefixes");
    public static final QName USE_LOCATION_HINTS = new QName("use-location-hints");
    public static final QName VALIDATE = new QName("validate");
    public static final QName VERSION = new QName("version");
    public static final QName WRAP_ERROR_LINES = new QName("wrap-error-lines");
    public static final QName WRAP_RESULT_LINES = new QName("wrap-result-lines");
    public static final QName WRAPPER = new QName("wrapper");
    public static final QName WRAPPER_PREFIX = new QName("wrapper-prefix");
    public static final QName WRAPPER_NAMESPACE = new QName("wrapper-namespace");

    private XProcOptions()
    {
        // No instantiation
    }
}
