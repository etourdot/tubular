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
package org.trancecode.xproc.parser;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcNamespaces;

/**
 * Elements from the XProc XML model.
 * 
 * @author Herve Quiroz
 */
public final class XProcElements
{
    public static final QName CHOOSE = XProcNamespaces.XPROC.newSaxonQName("choose");
    public static final QName DECLARE_STEP = XProcNamespaces.XPROC.newSaxonQName("declare-step");
    public static final QName DOCUMENT = XProcNamespaces.XPROC.newSaxonQName("document");
    public static final QName EMPTY = XProcNamespaces.XPROC.newSaxonQName("empty");
    public static final QName FOR_EACH = XProcNamespaces.XPROC.newSaxonQName("for-each");
    public static final QName IMPORT = XProcNamespaces.XPROC.newSaxonQName("import");
    public static final QName INLINE = XProcNamespaces.XPROC.newSaxonQName("inline");
    public static final QName INPUT = XProcNamespaces.XPROC.newSaxonQName("input");
    public static final QName ITERATION_SOURCE = XProcNamespaces.XPROC.newSaxonQName("iteration-source");
    public static final QName LIBRARY = XProcNamespaces.XPROC.newSaxonQName("library");
    public static final QName OPTION = XProcNamespaces.XPROC.newSaxonQName("option");
    public static final QName OTHERWISE = XProcNamespaces.XPROC.newSaxonQName("otherwise");
    public static final QName OUTPUT = XProcNamespaces.XPROC.newSaxonQName("output");
    public static final QName PIPE = XProcNamespaces.XPROC.newSaxonQName("pipe");
    public static final QName PIPELINE = XProcNamespaces.XPROC.newSaxonQName("pipeline");
    public static final QName VARIABLE = XProcNamespaces.XPROC.newSaxonQName("variable");
    public static final QName WHEN = XProcNamespaces.XPROC.newSaxonQName("when");
    public static final QName WITH_OPTION = XProcNamespaces.XPROC.newSaxonQName("with-option");
    public static final QName WITH_PARAM = XProcNamespaces.XPROC.newSaxonQName("with-param");
    public static final QName XPATH_CONTEXT = XProcNamespaces.XPROC.newSaxonQName("xpath-context");

    public static final Set<QName> ELEMENTS_CORE_STEPS = ImmutableSet.of(FOR_EACH, CHOOSE);
    public static final Set<QName> ELEMENTS_DECLARE_STEP_OR_PIPELINE = ImmutableSet.of(DECLARE_STEP, PIPELINE);
    public static final Set<QName> ELEMENTS_IN_PIPELINE = ImmutableSet.of(IMPORT, PIPELINE);
    public static final Set<QName> ELEMENTS_IN_PIPELINE_LIBRARY = ImmutableSet.of(IMPORT, DECLARE_STEP, PIPELINE);
    public static final Set<QName> ELEMENTS_PORT_BINDINGS = ImmutableSet.of(INLINE, DOCUMENT, EMPTY, PIPE);
    public static final Set<QName> ELEMENTS_INPUT_PORTS = ImmutableSet.of(INPUT, ITERATION_SOURCE, XPATH_CONTEXT);
    public static final Set<QName> ELEMENTS_OUTPUT_PORTS = ImmutableSet.of(OUTPUT);
    public static final Set<QName> ELEMENTS_PORTS = ImmutableSet.copyOf(Iterables.concat(ELEMENTS_INPUT_PORTS,
            ELEMENTS_OUTPUT_PORTS));
    public static final Set<QName> ELEMENTS_VARIABLES = ImmutableSet.of(VARIABLE, OPTION, WITH_OPTION, WITH_PARAM);
    public static final Set<QName> ELEMENTS_STANDARD_PORTS = ImmutableSet.of(INPUT, OUTPUT);
    public static final Set<QName> ELEMENTS_ROOT = ImmutableSet.of(PIPELINE, LIBRARY, DECLARE_STEP);
    public static final Set<QName> ELEMENTS_WHEN_OTHERWISE = ImmutableSet.of(WHEN, OTHERWISE);

    private XProcElements()
    {
        // No instantiation
    }

    public static XdmNode newParameterElement(final QName name, final String value, final Processor processor)
    {
        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
        builder.startDocument();
        // TODO use constants
        builder.startElement(XProcNamespaces.XPROC_STEP.newSaxonQName("param"));
        builder.namespace(XProcNamespaces.XPROC_STEP.prefix(), XProcNamespaces.XPROC_STEP.uri());
        builder.attribute(new QName("name"), name.toString());
        builder.attribute(new QName("value"), value.toString());
        builder.text(value);
        builder.endElement();
        builder.endDocument();

        return builder.getNode();
    }

    public static XdmNode newResultElement(final String value, final Processor processor)
    {
        final SaxonBuilder builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
        builder.startDocument();
        // TODO use constants
        builder.startElement(XProcNamespaces.XPROC_STEP.newSaxonQName("result"));
        builder.namespace(XProcNamespaces.XPROC_STEP.prefix(), XProcNamespaces.XPROC_STEP.uri());
        builder.text(value);
        builder.endElement();
        builder.endDocument();

        return builder.getNode();
    }
}
