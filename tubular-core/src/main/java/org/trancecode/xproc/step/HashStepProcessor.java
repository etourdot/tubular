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
import java.util.zip.CRC32;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.AbstractSaxonProcessorDelegate;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonProcessorDelegates;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Emmanuel Tourdot
 */
@ExternalResources(read = false, write = false)
public final class HashStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(HashStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.HASH;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String value = input.getOptionValue(XProcOptions.VALUE);
        assert value != null;
        LOG.trace("value = {}", value);
        final String algorithm = input.getOptionValue(XProcOptions.ALGORITHM);
        assert algorithm != null;
        LOG.trace("algorithm = {}", algorithm);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        assert match != null;
        LOG.trace("match = {}", match);
        final String version = input.getOptionValue(XProcOptions.VERSION);
        LOG.trace("version = {}", version);

        final String hashValue;
        if (StringUtils.equalsIgnoreCase("crc", algorithm))
        {
            if ("32".equals(version) || version == null)
            {
                final CRC32 crc32 = new CRC32();
                crc32.update(value.getBytes());
                hashValue = Long.toHexString(crc32.getValue());
            }
            else
            {
                throw XProcExceptions.xc0036(input.getLocation());
            }
        }
        else if (StringUtils.equalsIgnoreCase("md", algorithm))
        {
            if ("5".equals(version) || version == null)
            {
                hashValue = DigestUtils.md5Hex(value);
            }
            else
            {
                throw XProcExceptions.xc0036(input.getLocation());
            }
        }
        else if (StringUtils.equalsIgnoreCase("sha", algorithm))
        {
            if ("1".equals(version)|| version == null)
            {
                hashValue = DigestUtils.shaHex(value);
            }
            else if ("256".equals(version))
            {
                hashValue = DigestUtils.sha256Hex(value);
            }
            else if ("384".equals(version))
            {
                hashValue = DigestUtils.sha384Hex(value);
            }
            else if ("512".equals(version))
            {
                hashValue = DigestUtils.sha512Hex(value);
            }
            else
            {
                throw XProcExceptions.xc0036(input.getLocation());
            }
        }
        else
        {
            throw XProcExceptions.xc0036(input.getLocation());
        }

        final SaxonProcessorDelegate hashDelegate = new AbstractSaxonProcessorDelegate()
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
                builder.text(hashValue);
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
                builder.attribute(node.getNodeName(), hashValue);
            }

            @Override
             public void comment(final XdmNode node, final SaxonBuilder builder)
            {
                builder.comment(hashValue);
            }

            @Override
            public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
            {
                builder.processingInstruction(node.getNodeName().getLocalName(), hashValue);
            }

            @Override
            public void text(final XdmNode node, final SaxonBuilder builder)
            {
                builder.text(hashValue);
            }
        };

        final SaxonProcessor hashProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), hashDelegate, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = hashProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }
    
}
