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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.*;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@code p:replace}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.replace">p:replace</a>
 */
public final class ReplaceStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ReplaceStepProcessor.class);
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.COMMENT,
            XdmNodeKind.PROCESSING_INSTRUCTION, XdmNodeKind.TEXT);

    @Override
    public QName getStepType()
    {
        return XProcSteps.REPLACE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final XdmNode replacement = input.readNode(XProcPorts.REPLACEMENT);

        final String match = input.getOptionValue(XProcOptions.MATCH);
        assert match != null;
        LOG.trace("match = {}", match);

        final SaxonProcessorDelegate replaceDelegate = new AbstractSaxonProcessorDelegate()
        {
            private void replace(final SaxonBuilder builder)
            {
                builder.nodes(replacement);
            }

            @Override
            public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
            {
                builder.nodes(replacement);
                return false;
            }

            @Override
            public void endDocument(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode element, final SaxonBuilder builder)
            {
                builder.nodes(replacement);
                return EnumSet.noneOf(NextSteps.class);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void text(XdmNode node, SaxonBuilder builder)
            {
                builder.nodes(replacement);
            }

            @Override
            public void comment(XdmNode node, SaxonBuilder builder)
            {
                builder.nodes(replacement);
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                builder.nodes(replacement);
            }
        };

        final SaxonProcessorDelegate replaceWithError = SaxonProcessorDelegates.forNodeKinds(NODE_KINDS, replaceDelegate,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, NODE_KINDS);
                    }
                }));

        final SaxonProcessor replaceProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), replaceWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = replaceProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }

}
