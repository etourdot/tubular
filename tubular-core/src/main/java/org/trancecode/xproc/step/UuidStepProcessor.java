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

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import com.fasterxml.uuid.UUIDGenerator;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.*;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Emmanuel Tourdot
 */
@ExternalResources(read = false, write = false)
public final class UuidStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(UuidStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.UUID;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String match = input.getOptionValue(XProcOptions.MATCH);
        assert match != null;
        LOG.trace("match = {}", match);
        String version = input.getOptionValue(XProcOptions.VERSION, "4");
        LOG.trace("version = {}", version);

        if (!"1".equals(version) && !"3".equals(version) && !"4".equals(version))
        {
            throw XProcExceptions.xc0060(input.getStep().getLocation());
        }
        UUID uuid = null;
        switch(Integer.parseInt(version))
        {
            case 1:
                final TimeBasedGenerator t_uuid_gen = Generators.timeBasedGenerator();
                uuid = t_uuid_gen.generate();
                break;
            case 3:
                final NameBasedGenerator n_uuid_gen;
                try
                {
                    n_uuid_gen = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL, MessageDigest.getInstance("MD5"));
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw XProcExceptions.xc0060(input.getStep().getLocation());
                }
                uuid = n_uuid_gen.generate("tubular_uuid");
                break;
            case 4:
                RandomBasedGenerator r_uuid_gen = Generators.randomBasedGenerator();
                uuid = r_uuid_gen.generate();
                break;
        }
        LOG.trace("uuid = {}", uuid.toString());
        final String textUUID = uuid.toString();

        final SaxonProcessorDelegate uuidReplace = new CopyingSaxonProcessorDelegate()
        {
            private void replace(final XdmNode node, final SaxonBuilder builder)
            {
                LOG.trace("{@method} node = {}", node.getNodeName());
                if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE)
                {
                    builder.attribute(node.getNodeName(), textUUID);
                }
                else
                {
                    builder.text(textUUID);
                }
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
            {
                replace(node, builder);
                return EnumSet.of(NextSteps.PROCESS_ATTRIBUTES, NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
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
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input.getStep()
                        .getNode(), uuidReplace, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = matchProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);
    }
}
