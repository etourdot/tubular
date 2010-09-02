/*
 * Copyright (C) 2008 Romain Deltour
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

import com.google.common.collect.Lists;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.CopyingSaxonProcessorDelegate;
import org.trancecode.xml.saxon.MatchSaxonProcessorDelegate;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonProcessor;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:add-attribute standard XProc step.
 * 
 * @author Romain Deltour
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.add-attribute">p:add-attribute</a>
 */
public class AddAttributeStepProcessor extends AbstractStepProcessor
{
    public static final AddAttributeStepProcessor INSTANCE = new AddAttributeStepProcessor();

    private static final Logger LOG = Logger.getLogger(AddAttributeStepProcessor.class);

    @Override
    protected Environment doRun(final Step step, final Environment environment)
    {
        LOG.trace("{@method} step = {}", step.getName());
        assert step.getType().equals(XProcSteps.ADD_ATTRIBUTE);

        final String match = environment.getVariable(XProcOptions.MATCH);
        final String attributeName = environment.getVariable(XProcOptions.ATTRIBUTE_NAME);
        final String attributePrefix = environment.getVariable(XProcOptions.ATTRIBUTE_PREFIX);
        final String attributeNamespace = environment.getVariable(XProcOptions.ATTRIBUTE_NAMESPACE);
        final String attributeValue = environment.getVariable(XProcOptions.ATTRIBUTE_VALUE);

        // Check required attributes are set
        assert match != null;
        LOG.trace("match = {}", match);
        assert attributeName != null;
        LOG.trace("attribute-name = {}", attributeName);
        assert attributeName != null;
        LOG.trace("attribute-value = {}", attributeValue);

        // FIXME what happens when there is an @attribute-prefix but no
        // @attribute-namespace ?
        if ((attributeNamespace != null || attributePrefix != null) && attributeName.contains(":"))
        {
            throw XProcExceptions.xd0034(step.getLocation());
        }

        // Create the attribute QName
        final QName attributeQName;
        if (attributeNamespace != null)
        {
            attributeQName = new QName((attributePrefix != null) ? attributePrefix : "", attributeNamespace,
                    attributeName);
        }
        else
        {
            attributeQName = new QName(attributeName, step.getNode());
        }

        // Check the step is not used for a new namespace declaration
        if ("http://www.w3.org/2000/xmlns/".equals(attributeQName.getNamespaceURI())
                || ("xmlns".equals(attributeQName.toString())))
        {
            throw XProcExceptions.xc0059(step.getLocation());
        }

        // TODO catch IllegalArgumentException to statically check the
        // XSLTMatchPattern ?
        final SaxonProcessor matchProcessor = new SaxonProcessor(environment.getConfiguration().getProcessor(),
                new MatchSaxonProcessorDelegate(environment.getConfiguration().getProcessor(), match,
                        new AddAttributeProcessorDelegate(step, attributeQName, attributeValue),
                        new CopyingSaxonProcessorDelegate()));
        final XdmNode inputDoc = environment.readNode(step.getPortReference(XProcPorts.SOURCE));
        final XdmNode result = matchProcessor.apply(inputDoc);
        return environment.writeNodes(step.getPortReference(XProcPorts.RESULT), result);
    }

    private static class AddAttributeProcessorDelegate extends AbstractMatchProcessorDelegate
    {

        private final QName attributeQName;
        private final String attributeValue;

        public AddAttributeProcessorDelegate(final Step step, final QName attributeQName, final String attributeValue)
        {
            super(step);
            this.attributeQName = attributeQName;
            this.attributeValue = attributeValue;
        }

        @Override
        public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
        {
            builder.startElement(node.getNodeName(), node);

            // Namespace Fixup
            QName fixedupQName = attributeQName;
            boolean isNamespaceDeclared = false;
            boolean shouldChangePrefix = false;
            final Iterator<XdmNode> inscopeNamespaces = SaxonAxis.namespaces(node).iterator();
            final List<String> inscopePrefixes = Lists.newLinkedList();
            // Check if the namespace is already declared...
            while (!isNamespaceDeclared && inscopeNamespaces.hasNext())
            {
                final XdmNode inscopeNamespace = inscopeNamespaces.next();
                final String inscopeNamespacePrefix = inscopeNamespace.getNodeName().getLocalName();
                final String inscopeNamespaceUri = inscopeNamespace.getStringValue();
                if (inscopeNamespaceUri.equals(fixedupQName.getNamespaceURI()))
                {
                    LOG.trace("Namespace {} already declared", fixedupQName.getNamespaceURI());
                    isNamespaceDeclared = true;
                    shouldChangePrefix = false;
                    if (!inscopeNamespacePrefix.equals(fixedupQName.getPrefix()))
                    {
                        LOG.trace("Prefix '{}' changed to existing prefix '{}'", fixedupQName.getNamespaceURI(),
                                inscopeNamespacePrefix);
                        fixedupQName = new QName(inscopeNamespacePrefix, inscopeNamespaceUri,
                                fixedupQName.getLocalName());
                    }
                }
                else if (inscopeNamespacePrefix.equals(fixedupQName.getPrefix()))
                {
                    LOG.trace("Prefix '{}' already in use for namespace '{}'", inscopeNamespacePrefix,
                            inscopeNamespaceUri);
                    shouldChangePrefix = true;
                }
                inscopePrefixes.add(inscopeNamespacePrefix);
            }
            // If the attribute namespace has no prefix, create a dummy one
            if (!isNamespaceDeclared && "".equals(fixedupQName.getPrefix())
                    && !"".equals(fixedupQName.getNamespaceURI()))
            {
                fixedupQName = new QName("ns", fixedupQName.getNamespaceURI(), fixedupQName.getLocalName());
                shouldChangePrefix = true;
            }
            if (shouldChangePrefix)
            {
                final int count = 1;
                String newPrefix = fixedupQName.getPrefix();
                while (shouldChangePrefix)
                {
                    newPrefix = newPrefix + count;
                    shouldChangePrefix = false;
                    final Iterator<String> prefixIterator = inscopePrefixes.iterator();
                    while (!shouldChangePrefix && prefixIterator.hasNext())
                    {
                        if (newPrefix.equals(prefixIterator.next()))
                        {
                            shouldChangePrefix = true;
                        }
                    }
                }
                fixedupQName = new QName(newPrefix, fixedupQName.getNamespaceURI(), fixedupQName.getLocalName());
            }

            // If the attribute namespace is not declared, explicitly declare it
            if (!isNamespaceDeclared)
            {
                builder.namespace(fixedupQName.getPrefix(), fixedupQName.getNamespaceURI());
            }

            // Do add the attribute
            builder.attribute(fixedupQName, attributeValue);

            // Add all the other attributes
            for (final XdmNode attribute : SaxonAxis.attributes(node))
            {
                if (!attribute.getNodeName().equals(fixedupQName))
                {
                    builder.attribute(attribute.getNodeName(), attribute.getStringValue());
                }
            }

            // Process the children, attributes have already been processed
            return EnumSet.of(NextSteps.PROCESS_CHILDREN, NextSteps.START_CONTENT);
        }
    }
}
