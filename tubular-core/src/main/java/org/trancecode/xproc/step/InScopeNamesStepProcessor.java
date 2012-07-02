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

import java.util.Map;

import net.sf.saxon.s9api.QName;
import org.trancecode.collection.TcMaps;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;

/**
 * {@code p:in-scope-names}.
 * 
 * @author Lucas Soltic
 * @see <a
 *      href="http://www.w3.org/TR/xproc-template/#c.in-scope-names">p:in-scope-names</a>
 */
@ExternalResources(read = false, write = false)
public final class InScopeNamesStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(InScopeNamesStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.IN_SCOPE_NAMES;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final Environment environment = Environment.getCurrentEnvironment();
        assert environment != null;

        final Map<QName, String> localVariables = environment.getLocalVariables();
        final Map<QName, String> inheritedVariables = environment.getInheritedVariables();
        final Map<QName, String> allVariables = TcMaps.merge(inheritedVariables, localVariables);

        LOG.trace("available variables = {}", allVariables);

        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.PARAM_SET);

        for (final Map.Entry<QName, String> entry : allVariables.entrySet())
        {
            LOG.trace("adding item {} to param-set", entry);

            builder.startElement(XProcXmlModel.Elements.PARAM);
            builder.attribute(XProcXmlModel.Attributes.NAME, entry.getKey().getLocalName());
            builder.attribute(XProcXmlModel.Attributes.VALUE, entry.getValue());
            builder.attribute(XProcXmlModel.Attributes.NAMESPACE, entry.getKey().getNamespaceURI());
            builder.endElement();
        }

        builder.endElement();
        builder.endDocument();

        LOG.trace("built node = {}", builder.getNode());

        output.writeNodes(XProcPorts.RESULT, builder.getNode());
    }
}
