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
import java.net.URI;
import java.net.URISyntaxException;
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
 * {@code p:make-absolute-uris}.
 * 
 * @author Emmanuel Tourdot
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.make-absolute-uris">p:make-absolute-uris</a>
 */
public final class MakeAbsoluteUrisStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(MakeAbsoluteUrisStepProcessor.class);
    private static final Set<XdmNodeKind> NODE_KINDS = ImmutableSet.of(XdmNodeKind.ELEMENT, XdmNodeKind.ATTRIBUTE);

    @Override
    public QName getStepType()
    {
        return XProcSteps.MAKE_ABSOLUTE_URIS;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        assert match != null;
        final String baseUriOption = input.getOptionValue(XProcOptions.BASE_URI);
        final URI baseUriURI = getUri(baseUriOption);

        final SaxonProcessorDelegate makeUrisDelegate = new AbstractSaxonProcessorDelegate()
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
                builder.startElement(element.getNodeName());
                for (final XdmNode attribute : SaxonAxis.attributes(element))
                {
                    LOG.trace("copy existing attribute: {}", attribute);
                    builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                }
                if (baseUriURI != null)
                {
                    builder.text(baseUriURI.resolve(element.getStringValue()).toString());
                }
                else
                {
                    builder.text(element.getBaseURI().toString());
                }
                return EnumSet.noneOf(NextSteps.class);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
            }

            @Override
            public void attribute(final XdmNode node, final SaxonBuilder builder)
            {
                if (baseUriURI != null)
                {
                    builder.attribute(node.getNodeName(), baseUriURI.resolve(node.getStringValue()).toString());
                }
                else
                {
                    builder.attribute(node.getNodeName(), node.getBaseURI().toString());
                }
            }
        };

        final SaxonProcessorDelegate makeUrisWithError = SaxonProcessorDelegates.forNodeKinds(NODE_KINDS,
                makeUrisDelegate, SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, NODE_KINDS);
                    }
                }));

        final SaxonProcessor makeUrisProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), makeUrisWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = makeUrisProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);

    }

    private static URI getUri(final String namespace)
    {
        if (namespace == null)
        {
            return null;
        }
        try
        {
            final URI uri = new URI(namespace);
            if (!uri.isAbsolute())
            {
                return null;
            }
            else
            {
                return uri.resolve(namespace);
            }
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }
}
