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

import com.google.common.collect.ImmutableMap;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:http-request standard XProc step.
 * 
 * @author Emmanuel Tourdot
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.http-request">p:http-request</a>
 */
public final class HttpRequestStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(HttpRequestStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.HTTP_REQUEST;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDoc = input.readNode(XProcPorts.SOURCE);
        final ImmutableMap.Builder<QName, String> defaultBuilder = new ImmutableMap.Builder<QName, String>();
        defaultBuilder.put(XProcOptions.CDATA_SECTION_ELEMENTS, "").put(XProcOptions.ESCAPE_URI_ATTRIBUTES, "false")
                .put(XProcOptions.INCLUDE_CONTENT_TYPE, "true").put(XProcOptions.INDENT, "false")
                .put(XProcOptions.METHOD, "xml").put(XProcOptions.NORMALIZATION_FORM, "none")
                .put(XProcOptions.OMIT_XML_DECLARATION, "true").put(XProcOptions.STANDALONE, "omit")
                .put(XProcOptions.VERSION, "1.0");
        final ImmutableMap<QName, String> defaultOptions = defaultBuilder.build();
        final ImmutableMap<String, Object> serializationOptions = StepUtils.getSerializationOptions(input, defaultOptions);

        final XdmNode request = SaxonAxis.childElement(sourceDoc);

        if (!XProcXmlModel.Elements.REQUEST.equals(request.getNodeName()))
        {
            throw XProcExceptions.xc0040(input.getStep().getLocation());
        }
        output.writeNodes(XProcPorts.RESULT, sourceDoc);
    }
}
