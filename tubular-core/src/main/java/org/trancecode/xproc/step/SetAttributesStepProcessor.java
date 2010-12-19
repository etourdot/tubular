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
package org.trancecode.xproc.step;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.XmlnsNamespace;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonBuilders;
import org.trancecode.xml.saxon.SaxonMaps;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:set-attributes}.
 * 
 * @author Herve Quiroz
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.set-attributes">p:set-attributes</a>
 */
public final class SetAttributesStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(SetAttributesStepProcessor.class);

    @Override
    public QName stepType()
    {
        return XProcSteps.SET_ATTRIBUTES;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode source = input.readNode(XProcPorts.SOURCE);
        final XdmNode attributesNode = input.readNode("attributes");
        final Map<QName, String> attributes = SaxonMaps.attributes(SaxonAxis.childElement(attributesNode));
        LOG.trace("attributes = {}", attributes);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        LOG.trace("match = {}", match);

        final SaxonProcessorDelegate setAttributes = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public EnumSet<NextSteps> startElement(final XdmNode element, final SaxonBuilder builder)
            {
                LOG.trace("element: {}", element.getNodeName());

                // copy the element
                builder.startElement(element.getNodeName(), element);

                // copy all existing attributes that don't need an update
                for (final XdmNode attribute : SaxonAxis.attributes(element))
                {
                    LOG.trace("copy existing attribute: {}", attribute);
                    if (!attributes.containsKey(attribute.getNodeName()))
                    {
                        builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                    }
                }

                // add new attributes
                for (final Entry<QName, String> attribute : attributes.entrySet())
                {
                    if (!XmlnsNamespace.instance().uri().equals(attribute.getKey().getNamespaceURI()))
                    {
                        final QName fixedName = SaxonBuilders.addAttribute(attribute.getKey(), attribute.getValue(),
                                element, builder);
                        LOG.trace("add attribute: {} with name {}", attribute, fixedName);
                    }
                }

                // process the children, attributes have already been processed
                return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
            }
        };

        final SaxonProcessorDelegate setAttributesForElements = SaxonProcessorDelegates.forNodeKinds(
                ImmutableSet.of(XdmNodeKind.ELEMENT), setAttributes,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, XdmNodeKind.ELEMENT);
                    }
                }));

        final SaxonProcessor matchProcessor = new SaxonProcessor(input.pipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.pipelineContext().getProcessor(), match, input.step()
                        .getNode(), setAttributesForElements, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = matchProcessor.apply(source);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
