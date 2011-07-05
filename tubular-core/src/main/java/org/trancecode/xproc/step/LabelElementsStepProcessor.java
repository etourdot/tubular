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

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
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
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:label-elements}.
 * 
 * @author Emmanuel Tourdot
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.label-elements">p:label-elements</a>
 */
@ExternalResources(read = false, write = false)
public final class LabelElementsStepProcessor extends AbstractStepProcessor
{
    private static final String ATTRIBUTE_DEFAULT_VALUE = "xml:id";
    private static final QName INDEX = new QName("p", XProcXmlModel.xprocNamespace().uri(), "index");

    @Override
    public QName getStepType()
    {
        return XProcSteps.LABEL_ELEMENTS;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDocument = input.readNode(XProcPorts.SOURCE);
        final String attributeOption = input.getOptionValue(XProcOptions.ATTRIBUTE, ATTRIBUTE_DEFAULT_VALUE);
        final String attributePrefixOption = input.getOptionValue(XProcOptions.ATTRIBUTE_PREFIX, null);
        final String attributeNamespaceOption = input.getOptionValue(XProcOptions.ATTRIBUTE_NAMESPACE, null);

        final QName attributeQName = Steps.getNewNamespace(attributePrefixOption, attributeNamespaceOption,
                attributeOption, input.getStep().getLocation(), input.getStep().getNode());

        final String labelOption = input.getOptionValue(XProcOptions.LABEL, "concat(\"_\",$p:index)");
        final String match = input.getOptionValue(XProcOptions.MATCH, "*");
        final boolean replaceOption = Boolean.parseBoolean(input.getOptionValue(XProcOptions.REPLACE, "true"));

        final SaxonProcessorDelegate labelElementsDelegate = new AbstractSaxonProcessorDelegate()
        {
            private final AtomicInteger countElement = new AtomicInteger(1);

            private String getNewLabel(final XdmNode element)
            {
                try
                {
                    final XPathCompiler xPathCompiler = element.getProcessor().newXPathCompiler();
                    xPathCompiler.declareNamespace(XProcXmlModel.xprocNamespace().prefix(), XProcXmlModel
                            .xprocNamespace().uri());
                    xPathCompiler.declareVariable(INDEX);
                    final XPathSelector xPathSelector = xPathCompiler.compile(labelOption).load();
                    xPathSelector.setVariable(INDEX, new XdmAtomicValue(countElement.get()));
                    xPathSelector.setContextItem(element);
                    final XdmItem item = xPathSelector.evaluateSingle();
                    return (item == null) ? "" : item.getStringValue();
                }
                catch (final SaxonApiException sae)
                {
                    return "";
                }
            }

            @Override
            public EnumSet<NextSteps> startElement(final XdmNode element, final SaxonBuilder builder)
            {
                builder.startElement(element.getNodeName(), element);
                for (final XdmNode attribute : SaxonAxis.attributes(element))
                {
                    if (!attributeQName.equals(attribute.getNodeName()) || !replaceOption)
                    {
                        builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                    }
                }
                builder.attribute(attributeQName, getNewLabel(element));
                countElement.addAndGet(1);
                return EnumSet.of(NextSteps.PROCESS_CHILDREN);
            }

            @Override
            public void endElement(final XdmNode node, final SaxonBuilder builder)
            {
                builder.endElement();
            }
        };

        final SaxonProcessorDelegate labelElementsWithError = SaxonProcessorDelegates.forNodeKinds(
                EnumSet.of(XdmNodeKind.ELEMENT), labelElementsDelegate,
                SaxonProcessorDelegates.error(new Function<XdmNode, XProcException>()
                {
                    @Override
                    public XProcException apply(final XdmNode node)
                    {
                        return XProcExceptions.xc0023(node, EnumSet.of(XdmNodeKind.ELEMENT));
                    }
                }));

        final SaxonProcessor labelElementsProcessor = new SaxonProcessor(input.getPipelineContext().getProcessor(),
                SaxonProcessorDelegates.forXsltMatchPattern(input.getPipelineContext().getProcessor(), match, input
                        .getStep().getNode(), labelElementsWithError, new CopyingSaxonProcessorDelegate()));

        final XdmNode result = labelElementsProcessor.apply(sourceDocument);
        output.writeNodes(XProcPorts.RESULT, result);

    }
}
