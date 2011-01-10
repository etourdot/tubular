/*
 * Copyright (C) 2010 Herve Quiroz
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

import net.sf.saxon.s9api.QName;
import org.trancecode.xml.Namespace;

/**
 * @author Herve Quiroz
 * @see <a
 *      href="http://lists.w3.org/Archives/Public/xproc-dev/2008Dec/0000.html">Submitting
 *      test suite results to tests.xproc.org</a>
 */
public final class XProcTestReportXmlModel
{
    private static final Namespace NAMESPACE = new Namespace("http://xproc.org/ns/testreport", "");

    public static final Namespace namespace()
    {
        return NAMESPACE;
    }

    public static final class Elements
    {
        public static final QName ACTUAL = namespace().newSaxonQName("actual");
        public static final QName DATE = namespace().newSaxonQName("date");
        public static final QName ERROR = namespace().newSaxonQName("error");
        public static final QName EXPECTED = namespace().newSaxonQName("expected");
        public static final QName FAIL = namespace().newSaxonQName("fail");
        public static final QName LANGUAGE = namespace().newSaxonQName("language");
        public static final QName MESSAGE = namespace().newSaxonQName("message");
        public static final QName NAME = namespace().newSaxonQName("name");
        public static final QName PASS = namespace().newSaxonQName("pass");
        public static final QName PROCESSOR = namespace().newSaxonQName("processor");
        public static final QName PSVI_SUPPORTED = namespace().newSaxonQName("psvi-supported");
        public static final QName TEST_REPORT = namespace().newSaxonQName("test-report");
        public static final QName TEST_SUITE = namespace().newSaxonQName("test-suite");
        public static final QName TITLE = namespace().newSaxonQName("title");
        public static final QName VENDOR = namespace().newSaxonQName("vendor");
        public static final QName VENDOR_URI = namespace().newSaxonQName("vendor-uri");
        public static final QName VERSION = namespace().newSaxonQName("version");
        public static final QName XPATH_VERSION = namespace().newSaxonQName("xpath-version");
        public static final QName XPROC_VERSION = namespace().newSaxonQName("xproc-version");

        private Elements()
        {
            // No instantiation
        }
    }

    public static final class Attributes
    {
        public static final QName EXPECTED = namespace().newSaxonQName("expected");
        public static final QName URI = namespace().newSaxonQName("uri");

        private Attributes()
        {
            // No instantiation
        }
    }

    private XProcTestReportXmlModel()
    {
        // No instantiation
    }
}
