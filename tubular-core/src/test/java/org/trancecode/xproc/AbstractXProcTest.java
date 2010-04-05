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

import org.trancecode.AbstractTest;
import org.trancecode.xml.saxon.SaxonUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.custommonkey.xmlunit.XMLAssert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractXProcTest extends AbstractTest
{
    @BeforeClass
    public static void setupLoggingLevel()
    {
        // setLoggingLevel("org.trancecode.xproc", TRACE);
    }

    @BeforeClass
    public static void parseStandardLibrary()
    {
        Logger.getLogger("org.trancecode").setLevel(Level.INFO);
        new PipelineFactory();
        Logger.getLogger("org.trancecode").setLevel(Level.TRACE);
    }

    protected void test(final URL testUrl) throws Exception
    {
        final PipelineFactory pipelineFactory = new PipelineFactory();
        final Processor processor = pipelineFactory.getProcessor();
        final Source testSource = new StreamSource(testUrl.toString());
        final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
        documentBuilder.setLineNumbering(true);
        final XdmNode testDocument = documentBuilder.build(testSource);
        final XdmNode testElement = SaxonUtil.childElement(testDocument, XProcTestSuiteXmlModel.ELEMENT_TEST);
        final String expectedError = testElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_ERROR);
        final XdmNode pipelineElement = SaxonUtil.childElement(testElement, XProcTestSuiteXmlModel.ELEMENT_PIPELINE);
        final XdmNode pipelineDocument;
        if (pipelineElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF) != null)
        {
            final String href = pipelineElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
            assert !href.isEmpty();
            pipelineDocument = documentBuilder.build(processor.getUnderlyingConfiguration().getURIResolver().resolve(
                    href, pipelineElement.getBaseURI().toString()));
        }
        else
        {
            pipelineDocument = SaxonUtil.childElement(pipelineElement);
        }
        final PipelineResult result;
        try
        {
            log.info("== parse pipeline ==");
            final Pipeline pipeline = pipelineFactory.newPipeline(pipelineDocument.asSource());
            log.info("== pipeline parsed ==");
            final RunnablePipeline runnablePipeline = pipeline.load();

            for (final XdmNode inputElement : SaxonUtil
                    .childElements(testElement, XProcTestSuiteXmlModel.ELEMENT_INPUT))
            {
                final String portName = inputElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
                final List<Source> sources = new ArrayList<Source>();
                for (final XdmNode node : SaxonUtil.childElements(inputElement))
                {
                    sources.add(getDocumentWrappedNode(node).asSource());
                }
                runnablePipeline.setPortBinding(portName, sources);
            }

            for (final XdmNode optionElement : SaxonUtil.childElements(testElement,
                    XProcTestSuiteXmlModel.ELEMENT_OPTION))
            {
                final QName name = SaxonUtil.getAttributeAsQName(optionElement, XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
                final String value = optionElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
                runnablePipeline.withOption(name, value);
            }

            for (final XdmNode parameterElement : SaxonUtil.childElements(testElement,
                    XProcTestSuiteXmlModel.ELEMENT_PARAMETER))
            {
                final QName name = SaxonUtil.getAttributeAsQName(parameterElement,
                        XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
                final String value = parameterElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
                runnablePipeline.withParam(name, value);
            }

            log.info("== run pipeline ==");
            result = runnablePipeline.run();
            log.info("== pipeline run ==");
        }
        catch (final XProcException e)
        {
            if (expectedError != null)
            {
                AssertJUnit.assertEquals(expectedError, "err:" + e.getLabel());
                return;
            }

            throw new IllegalStateException(e);
        }

        final XdmNode comparePipelineElement = Iterables.getOnlyElement(SaxonUtil.childElements(testElement,
                XProcTestSuiteXmlModel.ELEMENT_COMPARE_PIPELINE), null);
        if (comparePipelineElement != null)
        {
            // TODO parse compare-pipeline (if any)
            assert false : "TODO";
        }

        // TODO run compare-pipeline (if any)

        for (final XdmNode outputElement : SaxonUtil.childElements(testElement, XProcTestSuiteXmlModel.ELEMENT_OUTPUT))
        {
            final String portName = outputElement.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);

            final Iterable<XdmNode> expectedNodes = SaxonUtil.childElements(outputElement);
            final Iterable<XdmNode> actualNodes = result.readNodes(portName);
            AssertJUnit.assertEquals(portName + " = " + actualNodes.toString(), Iterables.size(expectedNodes),
                    Iterables.size(actualNodes));

            final Iterator<XdmNode> expectedNodesIterator = expectedNodes.iterator();
            final Iterator<XdmNode> actualNodesIterator = actualNodes.iterator();

            while (expectedNodesIterator.hasNext())
            {
                assert actualNodesIterator.hasNext();
                final XdmNode expectedNode = getDocumentWrappedNode(expectedNodesIterator.next());
                assertEquals(expectedNode, actualNodesIterator.next());
            }

            assert !actualNodesIterator.hasNext();
        }
    }

    private static XdmNode getDocumentWrappedNode(final XdmNode node)
    {
        if (node.getNodeName().equals(XProcTestSuiteXmlModel.ELEMENT_DOCUMENT))
        {
            return SaxonUtil.childElement(node);
        }

        return node;
    }

    private static void assertEquals(final XdmNode expected, final XdmNode actual)
    {
        assert expected != null;
        assert actual != null;
        final String message = String.format("expected:\n%s\nactual:\n%s", expected, actual);
        try
        {
            XMLAssert.assertXMLEqual(message, expected.toString(), actual.toString());
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(message, e);
        }
    }

    protected String getTestUrlPrefix()
    {
        return "http://svn.xproc.org/tests/";
    }

    protected void test(final String testName) throws Exception
    {
        final String testUrlString = getTestUrlPrefix() + testName;
        final URL testUrl;
        try
        {
            testUrl = new URL(testUrlString);
        }
        catch (final MalformedURLException e)
        {
            throw new IllegalArgumentException(testUrlString, e);
        }

        test(testUrl);
    }
}
