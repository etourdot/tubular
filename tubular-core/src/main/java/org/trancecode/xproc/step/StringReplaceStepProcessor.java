/*
 * Copyright (C) 2008 Herve Quiroz
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
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:string-replace}.
 * 
 * @author Herve Quiroz
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.string-replace">p:string-replace</a>
 */
@ExternalResources(read = false, write = false)
public final class StringReplaceStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(StringReplaceStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.STRING_REPLACE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        LOG.trace("match = {}", match);
        final String replace = input.getOptionValue(XProcOptions.REPLACE);
        LOG.trace("replace = {}", replace);

        final SaxonProcessorDelegate stringReplace = new AbstractSaxonProcessorDelegate()
        {
            private void replace(final XdmNode node, final SaxonBuilder builder)
            {
                LOG.trace("{@method} node = {}", node.getNodeName());
                final String text = input.evaluateXPath(replace, node).toString();
                LOG.trace("result = {}", text);
                if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE)
                {
                    builder.attribute(node.getNodeName(), text);
                }
                else
                {
                    builder.text(text);
                }
            }

            @Override
            public EnumSet<SaxonProcessorDelegate.NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
                return EnumSet.noneOf(SaxonProcessorDelegate.NextSteps.class);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
            }

            @Override
            public void comment(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
            }
        };
        final SaxonProcessor matchProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), stringReplace, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = matchProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
