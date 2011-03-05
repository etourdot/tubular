/*
 * Copyright (C) 2011 Emmanuel Tourdot
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

import java.util.EnumSet;

import javax.xml.XMLConstants;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.apache.commons.lang.StringUtils;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:add-xml-base standard XProc step.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.add-xml-base">p:add-xml-base</a>
 */
@ExternalResources(read = false, write = false)
public final class AddXmlBaseStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(AddXmlBaseStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.ADD_XML_BASE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDoc = input.readNode(XProcPorts.SOURCE);
        final boolean allOption = Boolean.valueOf(input.getOptionValue(XProcOptions.ALL, "false"));
        final boolean relativeOption = Boolean.valueOf(input.getOptionValue(XProcOptions.RELATIVE, "true"));
        final QName xmlBase = new QName(XMLConstants.XML_NS_URI, "base");

        if (allOption && relativeOption)
        {
            throw XProcExceptions.xc0058(input.getStep().getLocation());
        }

        final SaxonProcessorDelegate addXmlBaseDelegate = new CopyingSaxonProcessorDelegate()
        {
            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.startElement(node.getNodeName(), node);
                for (final XdmNode attribute : SaxonAxis.attributes(node))
                {
                    LOG.trace("copy existing attribute except xml base: {}", attribute);
                    if (!xmlBase.equals(attribute.getNodeName()))
                    {
                        builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                    }
                }
                if (allOption || XdmNodeKind.DOCUMENT.equals(node.getParent().getNodeKind()))
                {
                    builder.attribute(xmlBase, node.getBaseURI().toString());
                }
                else
                {
                    if (!node.getBaseURI().equals(node.getParent().getBaseURI()))
                    {
                        if (relativeOption)
                        {
                            final String attrBase = node.getParent().getBaseURI().resolve(node.getBaseURI()).toString();
                            final int lastIdx = StringUtils.lastIndexOf(attrBase, "/") + 1;
                            builder.attribute(xmlBase, StringUtils.substring(attrBase, lastIdx));
                        }
                        else
                        {
                            builder.attribute(xmlBase, node.getBaseURI().toString());
                        }
                    }
                }
                return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }
        };

        final SaxonProcessor addXmlBaseProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                addXmlBaseDelegate);
        final XdmNode result = addXmlBaseProcessor.apply(sourceDoc);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
