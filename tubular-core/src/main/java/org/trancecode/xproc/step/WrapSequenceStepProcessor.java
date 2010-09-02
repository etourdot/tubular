/*
 * Copyright (C) 2010 TranceCode Software
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
 */
package org.trancecode.xproc.step;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:wrap-sequence}.
 * 
 * @author Herve Quiroz
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.wrap-sequence">p:wrap-sequence</a>
 */
public final class WrapSequenceStepProcessor extends AbstractStepProcessor
{
    public static final WrapSequenceStepProcessor INSTANCE = new WrapSequenceStepProcessor();

    private static final Logger LOG = Logger.getLogger(WrapSequenceStepProcessor.class);

    @Override
    public QName stepType()
    {
        return XProcSteps.WRAP_SEQUENCE;
    }

    @Override
    protected Environment doRun(final Step step, final Environment environment) throws Exception
    {
        LOG.trace("{@method} step = {}", step.getName());
        assert step.getType().equals(XProcSteps.WRAP_SEQUENCE);

        final SaxonBuilder builder = new SaxonBuilder(environment.getConfiguration().getProcessor()
                .getUnderlyingConfiguration());

        final String wrapperLocalName = environment.getVariable(XProcOptions.WRAPPER);
        final String wrapperPrefix = environment.getVariable(XProcOptions.WRAPPER_PREFIX, null);
        final String wrapperNamespaceUri = environment.getVariable(XProcOptions.WRAPPER_NAMESPACE, null);
        final QName wrapper;
        if (wrapperPrefix == null || wrapperNamespaceUri == null)
        {
            wrapper = new QName(wrapperLocalName, step.getNode());
        }
        else
        {
            wrapper = new QName(wrapperPrefix, wrapperNamespaceUri, wrapperLocalName);
        }

        // TODO handle 'group-adjacent' option

        builder.startDocument();
        builder.startElement(wrapper, step.getNode());

        for (final XdmNode inputDocument : environment.readNodes(step.getPortReference(XProcPorts.SOURCE)))
        {
            builder.nodes(SaxonAxis.childElement(inputDocument));
        }

        builder.endElement();
        builder.endDocument();

        return environment.writeNodes(step.getPortReference(XProcPorts.RESULT), builder.getNode());
    }
}
