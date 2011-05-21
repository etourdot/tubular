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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.etourdot.xinclude.XIncProcEngine;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:xinclude}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.xinclude">p:xinclude</a>
 */
@ExternalResources(read = false, write = false)
public final class XIncludeStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(XIncludeStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.XINCLUDE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode node = input.readNode(XProcPorts.SOURCE);
        assert node != null;
        final boolean xmlBase = Boolean.parseBoolean(input.getOptionValue(XProcOptions.FIXUP_XML_BASE, "false"));
        LOG.trace("xmlBase = {}", xmlBase);
        final boolean xmlLang = Boolean.parseBoolean(input.getOptionValue(XProcOptions.FIXUP_XML_LANG, "false"));
        LOG.trace("xmlLang = {}", xmlLang);

        final Processor processor = input.getPipelineContext().getProcessor();
        final XIncProcEngine engine = new XIncProcEngine(processor);
        engine.getConfiguration().setBaseUrisFixup(xmlBase);
        engine.getConfiguration().setLanguageFixup(xmlLang);
        try
        {
            final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            final Serializer serializer = processor.newSerializer(baos1);
            serializer.serializeNode(node);
            final ByteArrayInputStream bais1 = new ByteArrayInputStream(baos1.toByteArray());
            final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            engine.parse(bais1, node.getBaseURI().toASCIIString(), baos2);
            final ByteArrayInputStream bais2 = new ByteArrayInputStream(baos2.toByteArray());
            output.writeNodes(XProcPorts.RESULT, processor.newDocumentBuilder().build(new StreamSource(bais2)));
        }
        catch (Exception e)
        {
            throw XProcExceptions.xc0029(SaxonLocation.of(node));
        }
    }
}
