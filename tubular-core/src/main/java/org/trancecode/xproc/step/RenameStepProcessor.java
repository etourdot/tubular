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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
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
 * {@code p:rename}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.rename">p:rename</a>
 */
public final class RenameStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(RenameStepProcessor.class);
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.ATTRIBUTE,
            XdmNodeKind.PROCESSING_INSTRUCTION);

    @Override
    public QName getStepType()
    {
        return XProcSteps.RENAME;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        final String new_name = input.getOptionValue(XProcOptions.NEW_NAME);
        final String new_prefix = input.getOptionValue(XProcOptions.NEW_PREFIX, null);
        final String new_namespace = input.getOptionValue(XProcOptions.NEW_NAMESPACE, null);

        assert match != null;
        LOG.trace("match = {}", match);
        assert new_name != null;
        LOG.trace("new_name = {}", new_name);

        final QName newName = getNewNamespace(new_prefix, new_namespace, new_name, input.getStep());
        final SaxonProcessorDelegate rename = new AbstractSaxonProcessorDelegate()
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
            public EnumSet<NextSteps> startElement(final XdmNode element, final SaxonBuilder builder)
            {
                builder.startElement(newName, element);
                for (final XdmNode attribute : SaxonAxis.attributes(element))
                {
                    LOG.trace("copy existing attribute: {}", attribute);
                    builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                }
                return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                builder.attribute(newName, node.getStringValue());
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                if (!"".equals(node.getNodeName().getNamespaceURI()))
                {
                    throw XProcExceptions.xc0013(node);
                }
                builder.processingInstruction(newName.getLocalName(), node.getStringValue());
            }
        };

        /**
         * Rule to apply before renaming: If the match option matches an
         * attribute and if the element on which it occurs already has an
         * attribute whose expanded name is the same as the expanded name of the
         * specified new-name, then the results is as if the current attribute
         * named "new-name" was deleted before renaming the matched attribute.
         */
        final SaxonProcessorDelegate deleteNewAttrib = new CopyingSaxonProcessorDelegate()
        {
            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                if (!node.getNodeName().equals(newName))
                {
                    super.attribute(node, builder);
                }
            }
        };
        final SaxonProcessorDelegate attributeDel = SaxonProcessorDelegates.forNodeKinds(
                ImmutableSet.of(XdmNodeKind.ATTRIBUTE), deleteNewAttrib, new CopyingSaxonProcessorDelegate());
        final SaxonProcessor delProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(), attributeDel);
        final XdmNode resultDel = delProcessor.apply(sourceDocument);

        // The renaming process itself
        final SaxonProcessorDelegate renameWithError = SaxonProcessorDelegates.forNodeKinds(NODE_KINDS, rename,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, NODE_KINDS);
                    }
                }));

        final SaxonProcessor matchProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), renameWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = matchProcessor.apply(resultDel);
        output.writeNodes(XProcPorts.RESULT, result);
    }

    private static QName getNewNamespace(final String new_prefix, final String new_namespace, final String new_name,
            final Step inputStep)
    {
        if (new_prefix != null)
        {
            if (new_namespace == null)
            {
                throw XProcExceptions.xd0034(inputStep.getLocation());
            }
            else
            {
                return new QName(new_prefix, new_namespace, new_name);
            }
        }
        else
        {
            if (new_namespace == null)
            {
                return new QName(new_name, inputStep.getNode());
            }
        }
        if (new_name.contains(":"))
        {
            throw XProcExceptions.xd0034(inputStep.getLocation());
        }
        else
        {
            return new QName("", new_namespace, new_name);
        }
    }
}
