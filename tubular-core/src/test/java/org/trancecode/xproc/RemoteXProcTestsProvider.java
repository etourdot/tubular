/*
 * Copyright (C) 2010 Romain Deltour
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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.testng.annotations.DataProvider;
import org.testng.collections.Lists;
import org.trancecode.xml.saxon.SaxonUtil;

/**
 * Provides the list of URLs to the XProc test files hosted on
 * http://tests.xproc.org/testsuite/ .
 * <p>
 * This provider finds the test URLs by parsing the XML returned by an HTTP GET
 * request on the test suite URL.
 * </p>
 * 
 * @author Romain Deltour
 * @author Herve Quiroz
 */
public class RemoteXProcTestsProvider
{
    public static final String PROPERTY_TEST_NAME_FILTER = "xproc.tests.filter";

    private final static String TEST_SUITE_URI_PATTERN = "http://tests.xproc.org/tests/%s/test-suite.xml";

    @DataProvider(name = "xprocTests")
    public static Object[][] listTests(final Method method) throws Exception
    {
        final List<Object[]> testUrls = Lists.newArrayList();

        final String testNameFilter = System.getProperty(PROPERTY_TEST_NAME_FILTER);

        final URI testSuiteUri = URI.create(String.format(TEST_SUITE_URI_PATTERN, method.getName()));
        final InputStream inputStream = testSuiteUri.toURL().openStream();
        try
        {
            final Processor processor = new Processor(false);
            final XdmNode testSuiteDocument = processor.newDocumentBuilder().build(new StreamSource(inputStream));
            final XdmNode testSuite = SaxonUtil.childElement(testSuiteDocument,
                    XProcTestSuiteXmlModel.ELEMENT_TEST_SUITE);
            for (final XdmNode test : SaxonUtil.childElements(testSuite, XProcTestSuiteXmlModel.ELEMENT_TEST))
            {
                final String href = test.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
                if (testNameFilter == null || testNameFilter.isEmpty() || href.matches(testNameFilter))
                {
                    final URI testUri = testSuiteUri.resolve(href);
                    testUrls.add(new Object[] { testUri.toURL() });
                }
            }
        }
        finally
        {
            inputStream.close();
        }

        return testUrls.toArray(new Object[][] {});
    }
}
