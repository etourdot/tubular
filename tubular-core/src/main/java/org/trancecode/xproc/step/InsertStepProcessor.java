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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:insert}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.insert">p:insert</a>
 */
public final class InsertStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(InsertStepProcessor.class);
    private static final Set<String> POSITIONS = ImmutableSet.of("first-child", "last-child", "before", "after");
    private static final Set<String> ELEMENT_ONLY_POSITIONS = ImmutableSet.of("first-child", "last-child");
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.TEXT,
            XdmNodeKind.PROCESSING_INSTRUCTION, XdmNodeKind.COMMENT);

    @Override
    public QName stepType()
    {
        return XProcSteps.INSERT;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final List<XdmNode> insertionNodes = ImmutableList.copyOf(input.readNodes("insertion"));
        LOG.trace("insertionNodes = {size}", insertionNodes);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        LOG.trace("match = {}", match);
        final String position = input.getOptionValue(XProcOptions.POSITION);
        LOG.trace("position = {}", position);
        Preconditions.checkArgument(POSITIONS.contains(position), "'%s' is not an allowed position (%s)", position,
                Joiner.on(", ").join(POSITIONS));

        final SaxonProcessorDelegate insert = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public EnumSet<NextSteps> startElement(final XdmNode element, final SaxonBuilder builder)
            {
                if (position.equals("before"))
                {
                    builder.nodes(insertionNodes);
                }

                builder.startElement(element.getNodeName(), element);
                for (final XdmNode attribute : SaxonAxis.attributes(element))
                {
                    LOG.trace("copy existing attribute: {}", attribute);
                    builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                }

                if (position.equals("first-child"))
                {
                    builder.nodes(insertionNodes);
                }

                return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                if (position.equals("last-child"))
                {
                    builder.nodes(insertionNodes);
                }

                builder.endElement();

                if (position.equals("after"))
                {
                    builder.nodes(insertionNodes);
                }
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
                if (ELEMENT_ONLY_POSITIONS.contains(position))
                {
                    throw XProcExceptions.xc0025(input.getLocation(), node, position);
                }

                if (position.equals("before"))
                {
                    builder.nodes(insertionNodes);
                }

                builder.text(node.getStringValue());

                if (position.equals("after"))
                {
                    builder.nodes(insertionNodes);
                }
            }

            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
                if (ELEMENT_ONLY_POSITIONS.contains(position))
                {
                    throw XProcExceptions.xc0025(input.getLocation(), node, position);
                }

                if (position.equals("before"))
                {
                    builder.nodes(insertionNodes);
                }

                builder.comment(node.getStringValue());

                if (position.equals("after"))
                {
                    builder.nodes(insertionNodes);
                }
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                if (ELEMENT_ONLY_POSITIONS.contains(position))
                {
                    throw XProcExceptions.xc0025(input.getLocation(), node, position);
                }

                if (position.equals("before"))
                {
                    builder.nodes(insertionNodes);
                }

                builder.processingInstruction(node.getNodeName().getLocalName(), node.getStringValue());

                if (position.equals("after"))
                {
                    builder.nodes(insertionNodes);
                }
            }
        };

        final SaxonProcessorDelegate insertWithError = SaxonProcessorDelegates.forNodeKinds(NODE_KINDS, insert,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, NODE_KINDS);
                    }
                }));

        final SaxonProcessor matchProcessor = new SaxonProcessor(input.pipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.pipelineContext().getProcessor(), match, input.step()
                        .getNode(), insertWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = matchProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
