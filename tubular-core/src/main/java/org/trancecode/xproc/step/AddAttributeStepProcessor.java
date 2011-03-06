/*
 * Copyright (C) 2008 Romain Deltour
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
package org.trancecode.xproc.step;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;

import javax.xml.XMLConstants;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonBuilders;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:add-attribute standard XProc step.
 * 
 * @author Romain Deltour
 * @author Herve Quiroz
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.add-attribute">p:add-attribute</a>
 */
public final class AddAttributeStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(AddAttributeStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.ADD_ATTRIBUTE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final String match = input.getOptionValue(XProcOptions.MATCH);
        final String attributeName = input.getOptionValue(XProcOptions.ATTRIBUTE_NAME);
        final String attributePrefix = input.getOptionValue(XProcOptions.ATTRIBUTE_PREFIX);
        final String attributeNamespace = input.getOptionValue(XProcOptions.ATTRIBUTE_NAMESPACE);
        final String attributeValue = input.getOptionValue(XProcOptions.ATTRIBUTE_VALUE);

        // Check required attributes are set
        assert match != null;
        LOG.trace("match = {}", match);
        assert attributeName != null;
        LOG.trace("attribute-name = {}", attributeName);
        LOG.trace("attribute-value = {}", attributeValue);

        // FIXME what happens when there is an @attribute-prefix but no
        // @attribute-namespace ?
        if ((attributeNamespace != null || attributePrefix != null) && attributeName.contains(":"))
        {
            throw XProcExceptions.xd0034(input.getStep().getLocation());
        }

        // Create the attribute QName
        final QName attributeQName;
        if (attributeNamespace != null)
        {
            attributeQName = new QName((attributePrefix != null) ? attributePrefix : "", attributeNamespace,
                    attributeName);
        }
        else
        {
            if (attributeName.contains(XMLConstants.XMLNS_ATTRIBUTE + ":"))
            {
                throw XProcExceptions.xc0059(input.getStep().getLocation());
            }
            attributeQName = new QName(attributeName, input.getStep().getNode());
        }

        // Check the step is not used for a new namespace declaration
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(attributeQName.getLocalName())
                || XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attributeQName.getNamespaceURI()))
        {
            throw XProcExceptions.xc0059(input.getStep().getLocation());
        }

        final SaxonProcessorDelegate addAttribute = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.startElement(node.getNodeName(), node);

                final QName fixedupQName = SaxonBuilders.addAttribute(attributeQName, attributeValue, node, builder);

                // Add all the other attributes
                for (final XdmNode attribute : SaxonAxis.attributes(node))
                {
                    if (!attribute.getNodeName().equals(fixedupQName))
                    {
                        builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                    }
                }

                // Process the children, attributes have already been processed
                return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
            }
        };
        final SaxonProcessorDelegate addAttributeForElements = SaxonProcessorDelegates.forNodeKinds(
                ImmutableSet.of(XdmNodeKind.ELEMENT), addAttribute,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, XdmNodeKind.ELEMENT);
                    }
                }));

        final SaxonProcessor matchProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), addAttributeForElements, new CopyingSaxonProcessorDelegate()));
        final XdmNode inputDoc = input.readNode(XProcPorts.SOURCE);
        final XdmNode result = matchProcessor.apply(inputDoc);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
