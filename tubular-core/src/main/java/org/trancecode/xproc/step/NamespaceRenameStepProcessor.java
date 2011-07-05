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
import org.apache.commons.lang.StringUtils;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:namespace-rename}.
 *
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.namespace-rename">p:namespace-rename</a>
 */
public class NamespaceRenameStepProcessor extends AbstractStepProcessor
{
    private static final String APPLY_TO_ALL = "all";
    private static final String APPLY_TO_ELEMENTS = "elements";
    private static final String APPLY_TO_ATTRIBUTE = "attributes";

    @Override
    public QName getStepType()
    {
        return XProcSteps.NAMESPACE_RENAME;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String from = input.getOptionValue(XProcOptions.FROM);
        final String to = input.getOptionValue(XProcOptions.TO);
        final String apply_to = input.getOptionValue(XProcOptions.APPLY_TO, APPLY_TO_ALL);
        if (StringUtils.equalsIgnoreCase(XMLConstants.XML_NS_URI, from) ||
                StringUtils.equalsIgnoreCase(XMLConstants.XML_NS_URI, to) ||
            StringUtils.equalsIgnoreCase(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, from) ||
                StringUtils.equalsIgnoreCase(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, to))
        {
            throw XProcExceptions.xc0014(sourceDocument);
        }

        final SaxonProcessorDelegate nsRename = new CopyingSaxonProcessorDelegate()
        {
            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                final QName newNodeTo;
                if (APPLY_TO_ATTRIBUTE.equals(apply_to))
                {
                    newNodeTo = node.getNodeName();
                }
                else
                {
                    if (StringUtils.isNotBlank(to))
                    {
                        if (node.getNodeName().getNamespaceURI().equals(from))
                        {
                            newNodeTo = Steps.getNewNamespace(node.getNodeName().getPrefix(), to, node.getNodeName()
                                    .getLocalName(), SaxonLocation.of(node), node);
                        }
                        else if (StringUtils.isNotBlank(from))
                        {
                            newNodeTo = node.getNodeName();                            
                        }
                        else
                        {
                            newNodeTo = Steps.getNewNamespace("", to, node.getNodeName().getLocalName(),
                                    SaxonLocation.of(node), node);
                        }
                    }
                    else
                    {
                        if (node.getNodeName().getNamespaceURI().equals(from))
                        {
                            newNodeTo = new QName(node.getNodeName().getLocalName());
                        }
                        else
                        {
                            newNodeTo = node.getNodeName();
                        }
                    }
                }
                builder.startElement(newNodeTo);
                return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                if (!APPLY_TO_ELEMENTS.equals(apply_to))
                {
                    final QName newNodeTo;
                    if (StringUtils.isNotBlank(to))
                    {
                        if (node.getNodeName().getNamespaceURI().equals(from))
                        {
                            newNodeTo = Steps.getNewNamespace(node.getNodeName().getPrefix(), to, node.getNodeName()
                                    .getLocalName(), SaxonLocation.of(node), node);
                        }
                        else if (StringUtils.isNotBlank(from) ||
                                 StringUtils.isNotBlank(node.getNodeName().getNamespaceURI()))
                        {
                            newNodeTo = node.getNodeName();
                        }
                        else
                        {
                            newNodeTo = Steps.getNewNamespace(null, to, node.getNodeName().getLocalName(),
                                    SaxonLocation.of(node), node);
                        }
                    }
                    else
                    {
                        if (node.getNodeName().getNamespaceURI().equals(from))
                        {
                            newNodeTo = new QName(node.getNodeName().getLocalName());
                        }
                        else
                        {
                            newNodeTo = node.getNodeName();
                        }
                    }
                    builder.attribute(newNodeTo, node.getStringValue());
                }
                else
                {
                    super.attribute(node, builder);
                }
            }
        };

        final SaxonProcessor processor = new SaxonProcessor(input.getPipelineContext().getProcessor(), nsRename);

        final XdmNode result = processor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
