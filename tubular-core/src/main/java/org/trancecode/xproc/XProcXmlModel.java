/*
 * Copyright (C) 2010 TranceCode Software
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
 */
package org.trancecode.xproc;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;

import net.sf.saxon.s9api.QName;
import org.trancecode.xml.Namespace;

/**
 * @author Herve Quiroz
 */
public final class XProcXmlModel
{
    private static final Namespace XPROC_NAMESPACE = new Namespace("http://www.w3.org/ns/xproc", "p");
    private static final Namespace XPROC_STEP_NAMESPACE = new Namespace("http://www.w3.org/ns/xproc-step", "c");

    public static Namespace xprocNamespace()
    {
        return XPROC_NAMESPACE;
    }

    public static Namespace xprocStepNamespace()
    {
        return XPROC_STEP_NAMESPACE;
    }

    public static final class Elements
    {
        public static final QName CHOOSE = xprocNamespace().newSaxonQName("choose");
        public static final QName DECLARE_STEP = xprocNamespace().newSaxonQName("declare-step");
        public static final QName DOCUMENT = xprocNamespace().newSaxonQName("document");
        public static final QName DOCUMENTATION = xprocNamespace().newSaxonQName("documentation");
        public static final QName EMPTY = xprocNamespace().newSaxonQName("empty");
        public static final QName FOR_EACH = xprocNamespace().newSaxonQName("for-each");
        public static final QName IMPORT = xprocNamespace().newSaxonQName("import");
        public static final QName INLINE = xprocNamespace().newSaxonQName("inline");
        public static final QName INPUT = xprocNamespace().newSaxonQName("input");
        public static final QName ITERATION_SOURCE = xprocNamespace().newSaxonQName("iteration-source");
        public static final QName LIBRARY = xprocNamespace().newSaxonQName("library");
        public static final QName LINE = xprocStepNamespace().newSaxonQName("line");
        public static final QName OPTION = xprocNamespace().newSaxonQName("option");
        public static final QName OTHERWISE = xprocNamespace().newSaxonQName("otherwise");
        public static final QName OUTPUT = xprocNamespace().newSaxonQName("output");
        public static final QName PIPE = xprocNamespace().newSaxonQName("pipe");
        public static final QName PIPEINFO = xprocNamespace().newSaxonQName("pipeinfo");
        public static final QName PIPELINE = xprocNamespace().newSaxonQName("pipeline");
        public static final QName RESULT = xprocStepNamespace().newSaxonQName("result");
        public static final QName VARIABLE = xprocNamespace().newSaxonQName("variable");
        public static final QName VIEWPORT_SOURCE = xprocNamespace().newSaxonQName("viewport-source");
        public static final QName WHEN = xprocNamespace().newSaxonQName("when");
        public static final QName WITH_OPTION = xprocNamespace().newSaxonQName("with-option");
        public static final QName WITH_PARAM = xprocNamespace().newSaxonQName("with-param");
        public static final QName XPATH_CONTEXT = xprocNamespace().newSaxonQName("xpath-context");

        public static final Set<QName> ELEMENTS_CORE_STEPS = ImmutableSet.of(FOR_EACH, CHOOSE);
        public static final Set<QName> ELEMENTS_DECLARE_STEP_OR_PIPELINE = ImmutableSet.of(DECLARE_STEP, PIPELINE);
        public static final Set<QName> ELEMENTS_IGNORED = ImmutableSet.of(DOCUMENTATION, PIPEINFO);
        public static final Set<QName> ELEMENTS_IN_PIPELINE = ImmutableSet.of(IMPORT, PIPELINE);
        public static final Set<QName> ELEMENTS_IN_PIPELINE_LIBRARY = ImmutableSet.of(IMPORT, DECLARE_STEP, PIPELINE);
        public static final Set<QName> ELEMENTS_PORT_BINDINGS = ImmutableSet.of(INLINE, DOCUMENT, EMPTY, PIPE);
        public static final Set<QName> ELEMENTS_INPUT_PORTS = ImmutableSet.of(INPUT, ITERATION_SOURCE, VIEWPORT_SOURCE,
                XPATH_CONTEXT);
        public static final Set<QName> ELEMENTS_OUTPUT_PORTS = ImmutableSet.of(OUTPUT);
        public static final Set<QName> ELEMENTS_PORTS = ImmutableSet.copyOf(Iterables.concat(ELEMENTS_INPUT_PORTS,
                ELEMENTS_OUTPUT_PORTS));
        public static final Set<QName> ELEMENTS_VARIABLES = ImmutableSet.of(VARIABLE, OPTION, WITH_OPTION, WITH_PARAM);
        public static final Set<QName> ELEMENTS_STANDARD_PORTS = ImmutableSet.of(INPUT, OUTPUT);
        public static final Set<QName> ELEMENTS_ROOT = ImmutableSet.of(PIPELINE, LIBRARY, DECLARE_STEP);
        public static final Set<QName> ELEMENTS_WHEN_OTHERWISE = ImmutableSet.of(WHEN, OTHERWISE);

        private Elements()
        {
            // No instantiation
        }
    }

    public static final class Attributes
    {
        public static final QName HREF = new QName("href");
        public static final QName KIND = new QName("kind");
        public static final QName NAME = new QName("name");
        public static final QName PORT = new QName("port");
        public static final QName PRIMARY = new QName("primary");
        public static final QName REQUIRED = new QName("required");
        public static final QName SELECT = new QName("select");
        public static final QName SEQUENCE = new QName("sequence");
        public static final QName STEP = new QName("step");
        public static final QName TEST = new QName("test");
        public static final QName TYPE = new QName("type");
        public static final QName VALUE = new QName("value");
        public static final QName VERSION = new QName("version");

        private Attributes()
        {
            // No instantiation
        }
    }

    private XProcXmlModel()
    {
        // No instantiation
    }
}
