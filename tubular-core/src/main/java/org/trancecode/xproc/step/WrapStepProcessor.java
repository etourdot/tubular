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
import java.util.concurrent.atomic.AtomicReference;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
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
@ExternalResources(read = false, write = false)
public final class WrapStepProcessor extends AbstractStepProcessor
{
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.DOCUMENT, XdmNodeKind.ELEMENT,
            XdmNodeKind.TEXT, XdmNodeKind.PROCESSING_INSTRUCTION, XdmNodeKind.COMMENT);
    private final AtomicReference<XdmItem> wrapAdjacent = new AtomicReference<XdmItem>(null);

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

        final QName newName = Steps.getNewNamespace(wrapperPrefix, wrapperNamespaceUri, wrapperLocalName, input
                .getStep().getLocation(), input.getStep().getNode());
        wrapAdjacent.set(null);

        final SaxonProcessorDelegate wrapDelegate = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
                doStartWrap(groupAdjacent, newName, node, builder);
                builder.comment(node.getStringValue());
                doEndWrap(groupAdjacent, builder);
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                doStartWrap(groupAdjacent, newName, node, builder);
                builder.processingInstruction(node.getNodeName().getLocalName(), node.getStringValue());
                doEndWrap(groupAdjacent, builder);
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
            public void endDocument(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                doStartWrap(groupAdjacent, newName, node, builder);
                builder.startElement(node.getNodeName(), node);
                return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
                doEndWrap(groupAdjacent, builder);
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
                doStartWrap(groupAdjacent, newName, node, builder);
                builder.text(node.getStringValue());
                doEndWrap(groupAdjacent, builder);
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

    private void doStartWrap(final String groupAdjacent, final QName newName, final XdmNode node,
            final SaxonBuilder builder)
    {
        if (groupAdjacent == null)
        {
            builder.startElement(newName);
        }
        else
        {
            if (wrapAdjacent.get() == null)
            {
                builder.startElement(newName);
                wrapAdjacent.set(getGroupAdjacent(groupAdjacent, node));
            }
            else
            {
                final XdmItem itemGroup = wrapAdjacent.get();
                final XdmItem currItem = getGroupAdjacent(groupAdjacent, node);
                if (!itemGroup.getStringValue().equals(currItem.getStringValue()))
                {
                    builder.endElement();
                    builder.startElement(newName);
                    wrapAdjacent.set(currItem);
                }
            }
        }
    }

    private void doEndWrap(final String groupAdjacent, final SaxonBuilder builder)
    {
        if (groupAdjacent == null)
        {
            builder.endElement();
        }
        else
        {
            if (wrapAdjacent.get() == null)
            {
                builder.endElement();
            }
        }
    }

    private XdmItem getGroupAdjacent(final String groupAdjacent, final XdmNode node)
    {
        try
        {
            final XPathCompiler xPathCompiler = node.getProcessor().newXPathCompiler();
            final XPathSelector xPathSelector = xPathCompiler.compile(groupAdjacent).load();
            xPathSelector.setContextItem(node);
            return xPathSelector.evaluateSingle();
        }
        catch (final SaxonApiException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
