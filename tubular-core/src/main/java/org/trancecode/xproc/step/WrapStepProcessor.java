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
 */
package org.trancecode.xproc.step;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.Set;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.apache.commons.lang.StringUtils;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:wrap}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.wrap">p:wrap</a>
 */
public final class WrapStepProcessor extends AbstractStepProcessor
{
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.DOCUMENT, XdmNodeKind.ELEMENT,
            XdmNodeKind.TEXT, XdmNodeKind.PROCESSING_INSTRUCTION, XdmNodeKind.COMMENT);

    @Override
    public QName getStepType()
    {
        return XProcSteps.WRAP;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        final String wrapperLocalName = input.getOptionValue(XProcOptions.WRAPPER);
        final String wrapperPrefix = input.getOptionValue(XProcOptions.WRAPPER_PREFIX, null);
        final String wrapperNamespaceUri = input.getOptionValue(XProcOptions.WRAPPER_NAMESPACE, null);
        final String groupAdjacent = input.getOptionValue(XProcOptions.GROUP_ADJACENT);

        final QName newName = StepUtils.getNewNamespace(wrapperPrefix, wrapperNamespaceUri, wrapperLocalName, input
                .getStep().getLocation(), input.getStep().getNode());

        final SaxonProcessorDelegate wrapDelegate = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                builder.attribute(node.getNodeName(), node.getStringValue());
            }

            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
                if (!prevInGroupAdjacent(groupAdjacent, node))
                {
                    builder.startElement(newName);
                }
                builder.comment(node.getStringValue());
                if (!nextInGroupAdjacent(groupAdjacent, node))
                {
                    builder.endElement();
                }
            }

            @Override
            public void endDocument(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
                if (!nextInGroupAdjacent(groupAdjacent, node))
                {
                    builder.endElement();
                }
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                if (!prevInGroupAdjacent(groupAdjacent, node))
                {
                    builder.startElement(newName);
                }
                builder.processingInstruction(node.getNodeName().getLocalName(), node.getStringValue());
                if (!nextInGroupAdjacent(groupAdjacent, node))
                {
                    builder.endElement();
                }
            }

            @Override
            public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
            {
                builder.startDocument();
                builder.startElement(newName);
                builder.startContent();
                builder.nodes(node);
                builder.endElement();
                builder.endDocument();
                return false;
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                if (!prevInGroupAdjacent(groupAdjacent, node))
                {
                    builder.startElement(newName);
                }
                builder.startElement(node.getNodeName(), node);
                return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
                if (!prevInGroupAdjacent(groupAdjacent, node))
                {
                    builder.startElement(newName);
                }
                builder.text(node.getStringValue());
                if (!nextInGroupAdjacent(groupAdjacent, node))
                {
                    builder.endElement();
                }
            }
        };

        final SaxonProcessorDelegate wrapWithError = SaxonProcessorDelegates.forNodeKinds(NODE_KINDS, wrapDelegate,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, NODE_KINDS);
                    }
                }));

        final SaxonProcessor wrapProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), wrapWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = wrapProcessor.apply(sourceDocument);

        output.writeNodes(XProcPorts.RESULT, result);
    }

    private boolean nextInGroupAdjacent(final String groupAdjacent, final XdmNode node)
    {
        return inGroupAdjacent(groupAdjacent, node, true);
    }

    private boolean prevInGroupAdjacent(final String groupAdjacent, final XdmNode node)
    {
        return inGroupAdjacent(groupAdjacent, node, false);
    }

    private boolean inGroupAdjacent(final String groupAdjacent, final XdmNode node, final boolean next)
    {
        if (groupAdjacent == null)
        {
            return false;
        }
        try
        {
            final XPathCompiler xPathCompiler = node.getProcessor().newXPathCompiler();
            final XPathSelector xPathSelector = xPathCompiler.compile(groupAdjacent).load();
            xPathSelector.setContextItem(node);
            final XdmItem item1 = xPathSelector.evaluateSingle();
            final XdmSequenceIterator iterator = next ? node.axisIterator(Axis.FOLLOWING_SIBLING) : node
                    .axisIterator(Axis.PRECEDING_SIBLING);
            final XdmItem item2 = getNextSkpNode(iterator, xPathSelector);
            return item2 != null && item1.getStringValue().equals(item2.getStringValue());
        }
        catch (SaxonApiException e)
        {
            // TODO throw real XProcException ?
            e.printStackTrace();
        }
        return false;
    }

    private XdmItem getNextSkpNode(final XdmSequenceIterator iterator, final XPathSelector xPathSelector)
    {
        while (iterator.hasNext())
        {
            final XdmNode itm = (XdmNode) iterator.next();
            if (XdmNodeKind.ELEMENT.equals(itm.getNodeKind())
                    || (XdmNodeKind.TEXT.equals(itm.getNodeKind()) && !""
                            .equals(StringUtils.trim(itm.getStringValue()))))
            {
                try
                {
                    xPathSelector.setContextItem(itm);
                    return xPathSelector.evaluateSingle();
                }
                catch (SaxonApiException e)
                {
                    // TODO is it correct ?
                    return null;
                }
            }
        }
        return null;
    }
}
