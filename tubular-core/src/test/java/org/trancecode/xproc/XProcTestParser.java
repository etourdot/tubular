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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonUtil;
import org.xml.sax.InputSource;

/**
 * Parses a {http://xproc.org/ns/testsuite}test element into an XProcTestCase.
 * 
 * @author Romain Deltour
 */
public class XProcTestParser
{

    private static final Logger LOG = Logger.getLogger(XProcTestParser.class);

    private final Processor processor;
    private final Source source;

    private String title;
    private XdmNode description;
    private boolean ignoreWhitespace = true;
    private XdmNode pipeline;
    private final Map<String, List<XdmNode>> inputs = Maps.newHashMap();
    private final Map<QName, String> options = Maps.newHashMap();
    private final Map<String, Map<QName, String>> parameters = Maps.newHashMap();
    private QName error;
    private final Map<String, List<XdmNode>> outputs = Maps.newHashMap();
    private XdmNode comparePipeline;

    public XProcTestParser(final Processor processor, final Source source)
    {
        this.processor = processor;
        this.source = source;
    }

    public void parse()
    {
        try
        {
            final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
            final XdmNode pipelineDocument = documentBuilder.build(source);
            final XdmNode rootNode = SaxonUtil.childElement(pipelineDocument);
            parseTest(rootNode);
        }
        catch (final SaxonApiException e)
        {
            throw new PipelineException(e);
        }
    }

    private void parseTest(final XdmNode node)
    {
        if (!node.getNodeName().equals(XProcTestSuiteXmlModel.ELEMENT_TEST))
        {
            unsupportedElement(node);
        }
        parseTitle(SaxonUtil.childElement(node, XProcTestSuiteXmlModel.ELEMENT_TITLE));
        parseIgnoreWhitespace(node);
        parseError(node);
        parseDescription(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_DESCRIPTION));
        parseInputs(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_INPUT));
        parseOptions(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_OPTION));
        parseParameters(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_PARAMETER));
        parsePipeline(SaxonUtil.childElement(node, XProcTestSuiteXmlModel.ELEMENT_PIPELINE));
        if (error == null)
        {
            parseComparePipeline(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_COMPARE_PIPELINE));
            parseOutputs(SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_OUTPUT));
        }

    }

    private void parseTitle(final XdmNode node)
    {
        title = node.getStringValue();
        LOG.trace("== Test: {} ==", title);
    }

    private void parseIgnoreWhitespace(final XdmNode node)
    {
        final String ignoreWhitespaceValue = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_IGNORE_WHITESPACE);
        if (ignoreWhitespaceValue != null)
        {
            LOG.trace("Ignore whitespace: {}", ignoreWhitespaceValue);
            ignoreWhitespace = !"false".equals(ignoreWhitespaceValue);
        }
    }

    private void parseError(final XdmNode node)
    {
        final String errorName = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_ERROR);
        error = (errorName != null) ? new QName(errorName, node) : null;
        if (error != null)
        {
            LOG.trace("Expected error: {}", error);
        }
    }

    private void parseDescription(final Iterable<XdmNode> nodes)
    {
        final Iterator<XdmNode> iter = nodes.iterator();
        if (iter.hasNext())
        {
            description = SaxonUtil.asDocumentNode(iter.next(), processor);
            LOG.trace("Description: parsed");
        }
        if (iter.hasNext())
        {
            LOG.warn("More than one description was found");
        }
    }

    private void parseInputs(final Iterable<XdmNode> nodes)
    {
        for (final XdmNode node : nodes)
        {
            final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
            if (port == null)
            {
                LOG.warn("Input with no port");
            }
            else
            {
                LOG.trace("Input for port {}", port);
                inputs.put(port, extractDocuments(node));
            }
        }
    }

    private void parseOptions(final Iterable<XdmNode> nodes)
    {
        for (final XdmNode node : nodes)
        {
            final String name = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
            final String value = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
            if (name == null || value == null)
            {
                LOG.warn("Invalid option: {}={}", name, value);
            }
            else
            {
                LOG.trace("Option: {}={}", name, value);
                options.put(new QName(name, node), value);
            }
        }
    }

    private void parseParameters(final Iterable<XdmNode> nodes)
    {
        for (final XdmNode node : nodes)
        {
            final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
            final String name = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_NAME);
            final String value = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_VALUE);
            if (name == null || value == null)
            {
                LOG.warn("Involid parameter on port '{}': {}={}", port, name, value);
            }
            else
            {
                LOG.trace("Parameter: [{}] {}={}", port, name, value);
                final Map<QName, String> portParams = (parameters.containsKey(port)) ? parameters.get(port)
                        : new HashMap<QName, String>();
                portParams.put(new QName(name, node), value);
                parameters.put(port, portParams);
            }
        }
    }

    private void parsePipeline(final XdmNode node)
    {
        pipeline = extractPipeline(node);
        LOG.trace("Parsed pipeline");
    }

    private void parseComparePipeline(final Iterable<XdmNode> nodes)
    {
        final Iterator<XdmNode> iter = nodes.iterator();
        if (iter.hasNext())
        {
            comparePipeline = extractPipeline(iter.next());
            LOG.trace("Parsed compare-pipeline");
        }
        if (iter.hasNext())
        {
            LOG.warn("More than one compare-pipeline was found");
        }
    }

    private void parseOutputs(final Iterable<XdmNode> nodes)
    {
        for (final XdmNode node : nodes)
        {
            final String port = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_PORT);
            if (port == null)
            {
                LOG.warn("Output with no port");
            }
            else
            {
                LOG.trace("Output for port {}", port);
                outputs.put(port, extractDocuments(node));
            }
        }
    }

    private List<XdmNode> extractDocuments(final XdmNode node)
    {
        final List<XdmNode> documents = Lists.newLinkedList();
        String href = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
        if (href != null)
        {
            documents.add(loadExternalDocument(href, node));
            LOG.trace("New external document");
        }
        else
        {
            Iterator<XdmNode> iter = SaxonUtil.childElements(node, XProcTestSuiteXmlModel.ELEMENT_DOCUMENT).iterator();
            if (iter.hasNext())
            {
                while (iter.hasNext())
                {
                    final XdmNode documentNode = iter.next();
                    href = documentNode.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
                    if (href != null)
                    {
                        documents.add(loadExternalDocument(href, documentNode));
                        LOG.trace("New external document");
                    }
                    else
                    {
                        documents.add(SaxonUtil.asDocumentNode(SaxonUtil.childElement(documentNode), processor));
                        LOG.trace("New wrapped document");
                    }
                }
            }
            else
            {
                iter = SaxonUtil.childElements(node).iterator();
                if (iter.hasNext())
                {
                    documents.add(SaxonUtil.asDocumentNode(iter.next(), processor));
                    LOG.trace("New inline document");
                }
                else
                {
                    LOG.trace("New empty document");
                }
            }
        }
        return documents;
    }

    private XdmNode extractPipeline(final XdmNode node)
    {
        final String href = node.getAttributeValue(XProcTestSuiteXmlModel.ATTRIBUTE_HREF);
        if (href != null)
        {
            LOG.trace("New external document");
            return loadExternalDocument(href, node);
        }
        else
        {
            return SaxonUtil.asDocumentNode(SaxonUtil.childElement(node), processor);
        }
    }

    private XdmNode loadExternalDocument(final String href, final XdmNode node)
    {

        final DocumentBuilder builder = processor.newDocumentBuilder();
        final URI uri = node.getBaseURI().resolve(href);
        final SAXSource source = new SAXSource(new InputSource(uri.toString()));
        try
        {
            return builder.build(source);
        }
        catch (final SaxonApiException e)
        {
            throw new IllegalStateException("Couldn't load document at " + uri);
        }
    }

    public XProcTestCase getTest()
    {
        return new XProcTestCase(title, description, ignoreWhitespace, pipeline, inputs, options, parameters, error,
                outputs, comparePipeline);
    }

    private void unsupportedElement(final XdmNode node)
    {
        throw new IllegalStateException("Unsupported element " + node.getNodeName().toString());
    }
}
