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
package org.trancecode.xproc;

import net.sf.saxon.s9api.QName;
import org.trancecode.xml.Namespace;

/**
 * @author Herve Quiroz
 */
public final class XProcTestSuiteXmlModel
{
    public static final String NAMESPACE_XPROC_TEST_SUITE_PREFIX = "t";
    public static final String NAMESPACE_XPROC_TEST_SUITE_URI = "http://xproc.org/ns/testsuite";
    public static final Namespace NAMESPACE_XPROC_TEST_SUITE = new Namespace(NAMESPACE_XPROC_TEST_SUITE_URI,
            NAMESPACE_XPROC_TEST_SUITE_PREFIX);

    public static final QName ELEMENT_TEST_SUITE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("test-suite");
    public static final QName ELEMENT_TEST = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("test");
    public static final QName ELEMENT_DOCUMENT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("document");
    public static final QName ELEMENT_INPUT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("input");
    public static final QName ELEMENT_PARAMETER = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("parameter");
    public static final QName ELEMENT_OPTION = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("option");
    public static final QName ELEMENT_OUTPUT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("output");
    public static final QName ELEMENT_PIPELINE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("pipeline");
    public static final QName ELEMENT_COMPARE_PIPELINE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("compare-pipeline");
    public static final QName ELEMENT_TITLE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("title");
    public static final QName ELEMENT_DESCRIPTION = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("description");

    public static final QName ATTRIBUTE_ERROR = new QName("error");
    public static final QName ATTRIBUTE_HREF = new QName("href");
    public static final QName ATTRIBUTE_PORT = new QName("port");
    public static final QName ATTRIBUTE_NAME = new QName("name");
    public static final QName ATTRIBUTE_VALUE = new QName("value");
    public static final QName ATTRIBUTE_IGNORE_WHITESPACE = new QName("ignore-whitespace-differences");

    private XProcTestSuiteXmlModel()
    {
        // No instantiation
    }
}
