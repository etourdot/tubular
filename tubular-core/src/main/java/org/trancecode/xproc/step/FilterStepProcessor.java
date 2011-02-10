/*
 * Copyright (C) 2008 Emmanuel Tourdot
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

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:filter}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.filter">p:filter</a>
 */
public final class FilterStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(FilterStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.FILTER;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String select = input.getOptionValue(XProcOptions.SELECT);

        assert select != null;
        LOG.trace("select = {}", select);

        final SaxonProcessorDelegate unmatchDelegate = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
            {
                return true;
            }

            @Override
            public void endDocument(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
            }
        };

        final SaxonProcessorDelegate matchDelegate = new AbstractSaxonProcessorDelegate()
        {
            @Override
            public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
            {
                builder.startDocument();
                return true;
            }

            @Override
            public void endDocument(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endDocument();
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.startElement(node.getNodeName(), node);
                for (final XdmNode attribute : SaxonAxis.attributes(node))
                {
                    builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                }
                builder.startContent();
                builder.nodes(SaxonAxis.childNodesNoAttributes(node));
                builder.endElement();
                return EnumSet.noneOf(NextSteps.class);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
            }

        };

        // TODO
        // it works but it not correct: select must be an xpathExpression not a
        // XSLTMatchPattern
        final SaxonProcessor matchProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), select, input
                        .getStep().getNode(), matchDelegate, unmatchDelegate));

        final XdmNode result = matchProcessor.apply(sourceDocument);
        if (result != null)
        {
            output.writeNodes(XProcPorts.RESULT, result);
        }
        else
        {
            final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                    .getUnderlyingConfiguration());
            builder.startDocument();
            builder.endDocument();
            output.writeNodes(XProcPorts.RESULT, builder.getNode());
        }
    }
}
