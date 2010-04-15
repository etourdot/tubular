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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;

import net.sf.saxon.s9api.QName;

/**
 * Standard XProc steps.
 * 
 * @author Herve Quiroz
 */
public final class XProcSteps
{
    // Core steps
    public static final QName CHOOSE = XProcNamespaces.XPROC.newSaxonQName("choose");
    public static final QName FOR_EACH = XProcNamespaces.XPROC.newSaxonQName("for-each");
    public static final QName GROUP = XProcNamespaces.XPROC.newSaxonQName("group");
    public static final QName OTHERWISE = XProcNamespaces.XPROC.newSaxonQName("otherwise");
    public static final QName PIPELINE = XProcNamespaces.XPROC.newSaxonQName("pipeline");
    public static final QName TRY = XProcNamespaces.XPROC.newSaxonQName("try");
    public static final QName WHEN = XProcNamespaces.XPROC.newSaxonQName("when");

    public static final Set<QName> CORE_STEPS = ImmutableSet
            .of(CHOOSE, FOR_EACH, GROUP, OTHERWISE, PIPELINE, TRY, WHEN);
    public static final Set<QName> WHEN_STEPS = ImmutableSet.of(OTHERWISE, WHEN);

    // Required steps
    public static final QName ADD_ATTRIBUTE = XProcNamespaces.XPROC.newSaxonQName("add-attribute");
    public static final QName ADD_XML_BASE = XProcNamespaces.XPROC.newSaxonQName("add-xml-base");
    public static final QName COMPARE = XProcNamespaces.XPROC.newSaxonQName("compare");
    public static final QName COUNT = XProcNamespaces.XPROC.newSaxonQName("count");
    public static final QName DELETE = XProcNamespaces.XPROC.newSaxonQName("delete");
    public static final QName DIRECTORY_LIST = XProcNamespaces.XPROC.newSaxonQName("directory-list");
    public static final QName ERROR = XProcNamespaces.XPROC.newSaxonQName("error");
    public static final QName ESCAPE_MARKUP = XProcNamespaces.XPROC.newSaxonQName("escape-markup");
    public static final QName FILTER = XProcNamespaces.XPROC.newSaxonQName("filter");
    public static final QName HTTP_REQUEST = XProcNamespaces.XPROC.newSaxonQName("http-request");
    public static final QName IDENTITY = XProcNamespaces.XPROC.newSaxonQName("identity");
    public static final QName INSERT = XProcNamespaces.XPROC.newSaxonQName("insert");
    public static final QName LABEL_ELEMENT = XProcNamespaces.XPROC.newSaxonQName("label-element");
    public static final QName LOAD = XProcNamespaces.XPROC.newSaxonQName("load");
    public static final QName MAKE_ABSOLUTE_URIS = XProcNamespaces.XPROC.newSaxonQName("make-absolute-uris");
    public static final QName NAMESPACE_RENAME = XProcNamespaces.XPROC.newSaxonQName("namespace-rename");
    public static final QName PACK = XProcNamespaces.XPROC.newSaxonQName("pack");
    public static final QName PARAMETERS = XProcNamespaces.XPROC.newSaxonQName("parameters");
    public static final QName RENAME = XProcNamespaces.XPROC.newSaxonQName("rename");
    public static final QName REPLACE = XProcNamespaces.XPROC.newSaxonQName("replace");
    public static final QName SET_ATTRIBUTES = XProcNamespaces.XPROC.newSaxonQName("set-attributes");
    public static final QName SINK = XProcNamespaces.XPROC.newSaxonQName("sink");
    public static final QName SPLIT_SEQUENCE = XProcNamespaces.XPROC.newSaxonQName("split-sequence");
    public static final QName STORE = XProcNamespaces.XPROC.newSaxonQName("store");
    public static final QName STRING_REPLACE = XProcNamespaces.XPROC.newSaxonQName("string-replace");
    public static final QName UNESCAPE_MARKUP = XProcNamespaces.XPROC.newSaxonQName("unescape-markup");
    public static final QName UNWRAP = XProcNamespaces.XPROC.newSaxonQName("unwrap");
    public static final QName WRAP = XProcNamespaces.XPROC.newSaxonQName("wrap");
    public static final QName XINCLUDE = XProcNamespaces.XPROC.newSaxonQName("xinclude");
    public static final QName XSLT = XProcNamespaces.XPROC.newSaxonQName("xslt");

    public static final Set<QName> REQUIRED_STEPS = ImmutableSet.of(ADD_ATTRIBUTE, ADD_XML_BASE, COMPARE, COUNT,
            DELETE, DIRECTORY_LIST, ERROR, ESCAPE_MARKUP, FILTER, HTTP_REQUEST, IDENTITY, INSERT, LABEL_ELEMENT, LOAD,
            MAKE_ABSOLUTE_URIS, NAMESPACE_RENAME, PACK, PARAMETERS, RENAME, REPLACE, SET_ATTRIBUTES, SINK,
            SPLIT_SEQUENCE, STORE, STRING_REPLACE, UNESCAPE_MARKUP, UNWRAP, WRAP, XINCLUDE, XSLT);

    // Optional steps
    public static final QName EXEC = XProcNamespaces.XPROC.newSaxonQName("exec");
    public static final QName HASH = XProcNamespaces.XPROC.newSaxonQName("hash");
    public static final QName UUID = XProcNamespaces.XPROC.newSaxonQName("uuid");
    public static final QName VALIDATE_WITH_RELANXNG = XProcNamespaces.XPROC.newSaxonQName("validate-with-relaxng");
    public static final QName VALIDATE_WITH_SCHEMATRON = XProcNamespaces.XPROC
            .newSaxonQName("validate-with-schematron");
    public static final QName VALIDATE_WITH_SCHEMA = XProcNamespaces.XPROC.newSaxonQName("validate-with-xml-schema");
    public static final QName WWW_FORM_URL_DECODE = XProcNamespaces.XPROC.newSaxonQName("www-form-url-decode");
    public static final QName WWW_FORM_URL_ENCODE = XProcNamespaces.XPROC.newSaxonQName("www-form-url-encode");
    public static final QName XQUERY = XProcNamespaces.XPROC.newSaxonQName("xquery");
    public static final QName XSL_FORMATTER = XProcNamespaces.XPROC.newSaxonQName("xsl-formatter");

    public static final Set<QName> OPTIONAL_STEPS = ImmutableSet.of(EXEC, HASH, UUID, VALIDATE_WITH_RELANXNG,
            VALIDATE_WITH_SCHEMA, VALIDATE_WITH_SCHEMATRON, WWW_FORM_URL_DECODE, WWW_FORM_URL_ENCODE, XQUERY,
            XSL_FORMATTER);

    public static final Set<QName> ALL_STEPS = ImmutableSet.copyOf(Iterables.concat(CORE_STEPS, REQUIRED_STEPS,
            OPTIONAL_STEPS));

    private XProcSteps()
    {
        // No instantiation
    }
}
