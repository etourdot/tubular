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

import com.google.common.collect.Iterables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.trancecode.AbstractTest;
import org.trancecode.xml.saxon.SaxonUtil;

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
        final XProcTestCase test = getTest(testUrl, processor);

        final PipelineResult result;
        try
        {
            log.info("== parse pipeline ==");
            final Pipeline pipeline = pipelineFactory.newPipeline(test.getPipeline().asSource());
            log.info("== pipeline parsed ==");
            final RunnablePipeline runnablePipeline = pipeline.load();

            // Set inputs
            for (final String port : test.getInputs().keySet())
            {
                final List<Source> sources = new ArrayList<Source>();
                for (final XdmNode inputDoc : test.getInputs().get(port))
                {
                    sources.add(inputDoc.asSource());
                }
                runnablePipeline.setPortBinding(port, sources);
            }

            // Set options
            for (final QName name : test.getOptions().keySet())
            {
                runnablePipeline.withOption(name, test.getOptions().get(name));
            }

            // Set parameters
            // TODO how to deal with multiple parameter ports ?
            for (final String port : test.getParameters().keySet())
            {
                final List<Source> sources = new ArrayList<Source>();
                for (final QName name : test.getParameters().get(port).keySet())
                {
                    runnablePipeline.withParam(name, test.getParameters().get(port).get(name));
                }
            }

            log.info("== run pipeline ==");
            result = runnablePipeline.run();
            log.info("== pipeline run ==");
        }
        catch (final XProcException e)
        {
            if (test.getError() != null)
            {
                // TODO enable other error prefixes
                AssertJUnit.assertEquals(test.getError(), "err:" + e.getLabel());
                return;
            }
            throw new IllegalStateException(e);
        }

        if (test.getComparePipeline() != null)
        {
            // TODO parse compare-pipeline (if any)
            assert false : "TODO";
        }

        // TODO run compare-pipeline (if any)

        for (final String port : test.getOutputs().keySet())
        {

            final Iterable<XdmNode> actualNodes = result.readNodes(port);
            final Iterable<XdmNode> expectedNodes = test.getOutputs().get(port);
            AssertJUnit.assertEquals(port + " = " + actualNodes.toString(), Iterables.size(expectedNodes), Iterables
                    .size(actualNodes));

            final Iterator<XdmNode> expectedNodesIterator = expectedNodes.iterator();
            final Iterator<XdmNode> actualNodesIterator = actualNodes.iterator();
            while (expectedNodesIterator.hasNext())
            {
                assert actualNodesIterator.hasNext();
                final XdmNode expectedNode = expectedNodesIterator.next();
                final XdmNode actualNode = actualNodesIterator.next();
                assertEquals(expectedNode, actualNode, processor, test.isIgnoreWhitespace());
            }
            assert !actualNodesIterator.hasNext();
        }
    }

    private XProcTestCase getTest(final URL testUrl, final Processor processor)
    {
        final Source source = new StreamSource(testUrl.toString());
        final XProcTestParser parser = new XProcTestParser(processor, source);
        parser.parse();
        return parser.getTest();
    }

    private static void assertEquals(final XdmNode expected, final XdmNode actual, final Processor processor,
            final boolean ignoreWhitespace)
    {
        assert expected != null;
        assert actual != null;
        final XdmNode docExpected = SaxonUtil.asDocumentNode(expected, processor);
        final XdmNode docActual = SaxonUtil.asDocumentNode(actual, processor);
        final String message = String.format("expected:\n%s\nactual:\n%s", docExpected, docActual);
        try
        {
            XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
            XMLAssert.assertXMLEqual(message, docExpected.toString(), docActual.toString());
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
