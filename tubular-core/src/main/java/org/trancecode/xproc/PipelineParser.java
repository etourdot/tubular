/*
 * Copyright (C) 2008 TranceCode Software
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
package org.trancecode.xproc;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.XProcXmlModel.Attributes;
import org.trancecode.xproc.XProcXmlModel.Elements;
import org.trancecode.xproc.binding.DocumentPortBinding;
import org.trancecode.xproc.binding.EmptyPortBinding;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.binding.PipePortBinding;
import org.trancecode.xproc.binding.PortBinding;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.Port.Type;
import org.trancecode.xproc.port.PortReference;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.step.PipelineStepProcessor;
import org.trancecode.xproc.step.Step;
import org.trancecode.xproc.step.StepProcessor;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public class PipelineParser
{
    private static final Logger LOG = Logger.getLogger(PipelineParser.class);
    private static Set<String> EMPTY_LIBRARIES = ImmutableSet.of();

    private final Processor processor;
    private final Source source;
    private final Map<QName, Step> importedLibrary;
    private final Map<QName, Step> localLibrary = Maps.newHashMap();
    private final Map<QName, StepProcessor> stepProcessors;
    private final Set<String> libraries = Sets.newHashSet();

    private Step mainPipeline;
    private XdmNode rootNode;

    public PipelineParser(final Processor processor, final Source source, final Map<QName, Step> library,
            final Map<QName, StepProcessor> stepProcessors)
    {
        this(processor, source, library, stepProcessors, EMPTY_LIBRARIES);
    }

    private PipelineParser(final Processor processor, final Source source, final Map<QName, Step> library,
            final Map<QName, StepProcessor> stepProcessors, final Set<String> libraries)
    {
        assert processor != null;
        this.processor = processor;

        assert source != null;
        this.source = source;

        assert library != null;
        this.importedLibrary = library;

        assert stepProcessors != null;
        this.stepProcessors = stepProcessors;

        this.libraries.addAll(libraries);
    }

    public void parse()
    {
        try
        {
            final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
            documentBuilder.setLineNumbering(true);
            final XdmNode pipelineDocument = documentBuilder.build(source);
            rootNode = SaxonAxis.childElement(pipelineDocument, Elements.ELEMENTS_ROOT);

            parseImports(rootNode);

            if (Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE.contains(rootNode.getNodeName()))
            {
                mainPipeline = parseDeclareStep(rootNode);
            }
            else if (rootNode.getNodeName().equals(Elements.LIBRARY))
            {
                parseDeclareSteps(rootNode);
            }
            else
            {
                unsupportedElement(rootNode);
            }
        }
        catch (final SaxonApiException e)
        {
            throw new PipelineException(e);
        }
    }

    private void declareStep(final Step step)
    {
        localLibrary.put(step.getType(), step);
    }

    private void parseDeclareSteps(final XdmNode node)
    {
        for (final XdmNode stepNode : SaxonAxis.childElements(node, Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE))
        {
            parseDeclareStep(stepNode);
        }
    }

    private Step parseDeclareStep(final XdmNode stepNode)
    {
        final QName type = Saxon.getAttributeAsQName(stepNode, Attributes.TYPE);
        LOG.trace("new step type: {}", type);

        Step step;
        if (stepProcessors.containsKey(type))
        {
            final StepProcessor stepProcessor = stepProcessors.get(type);
            step = Step.newStep(stepNode, type, stepProcessor, false);
        }
        else
        {
            final String name = getStepName(stepNode);
            step = PipelineStepProcessor.newPipeline(type);
            step = step.setName(name);
        }

        step = step.setLocation(getLocation(stepNode));

        if (PipelineStepProcessor.isPipeline(step))
        {
            step = PipelineStepProcessor.addImplicitPorts(step);
        }

        step = parseStepChildNodes(stepNode, step);

        declareStep(step);
        return step;
    }

    private Step parseStepChildNodes(final XdmNode stepNode, final Step step)
    {
        Step configuredStep = step;
        for (final XdmNode node : SaxonAxis.childNodes(stepNode))
        {
            configuredStep = parseStepChildNode(node, configuredStep);
        }

        return configuredStep;
    }

    private Step parseStepChildNode(final XdmNode node, final Step step)
    {
        if (node.getNodeKind() == XdmNodeKind.ELEMENT)
        {
            if (Elements.ELEMENTS_PORTS.contains(node.getNodeName()))
            {
                final String portName = getPortName(node);
                if (step.getPorts().containsKey(portName))
                {
                    return parseWithPort(node, step);
                }
                else
                {
                    return parseDeclarePort(node, step);
                }
            }

            if (node.getNodeName().equals(Elements.VARIABLE))
            {
                return parseDeclareVariable(node, step);
            }

            if (node.getNodeName().equals(Elements.OPTION))
            {
                return parseOption(node, step);
            }

            if (node.getNodeName().equals(Elements.WITH_OPTION))
            {
                return parseWithOption(node, step);
            }

            if (node.getNodeName().equals(Elements.WITH_PARAM))
            {
                return parseWithParam(node, step);
            }

            if (getSupportedStepTypes().contains(node.getNodeName()))
            {
                return step.addChildStep(parseInstanceStep(node));
            }

            if (Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE.contains(node.getNodeName()))
            {
                parseDeclareStep(node);
                return step;
            }

            LOG.warn("child element not supported: {}", node.getNodeName());
        }
        else if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE)
        {
            LOG.trace("{} = {}", node.getNodeName(), node.getStringValue());
            final QName name = node.getNodeName();
            final String value = node.getStringValue();

            if (name.getNamespaceURI().isEmpty() && !name.equals(Attributes.NAME) && !name.equals(Attributes.TYPE)
                    && !name.equals(Attributes.VERSION))
            {
                if (!step.hasOptionDeclared(name))
                {
                    throw XProcExceptions.xs0031(getLocation(node), name, step.getType());
                }
                return step.withOptionValue(name, value);
            }
        }
        else if (node.getNodeKind() == XdmNodeKind.TEXT)
        {
            if (!node.getStringValue().trim().isEmpty())
            {
                LOG.trace("unexpected text node: {}", node.getStringValue());
            }
        }
        else
        {
            LOG.warn("child node not supported: {}", node.getNodeKind());
        }

        return step;
    }

    private Step parseWithPort(final XdmNode portNode, final Step step)
    {
        final String portName = getPortName(portNode);
        final Port port = step.getPort(portName);
        assert port.isParameter() || port.getType().equals(getPortType(portNode)) : "port = " + port.getType()
                + " ; with-port = " + getPortType(portNode);

        final String select = portNode.getAttributeValue(Attributes.SELECT);
        LOG.trace("select = {}", select);

        final Port configuredPort = port.setSelect(select).setPortBindings(parsePortBindings(portNode));

        LOG.trace("step {} with port {}", step, port);

        return step.withPort(configuredPort);
    }

    private String getPortName(final XdmNode portNode)
    {
        if (Elements.ELEMENTS_STANDARD_PORTS.contains(portNode.getNodeName()))
        {
            return portNode.getAttributeValue(Attributes.PORT);
        }

        if (portNode.getNodeName().equals(Elements.ITERATION_SOURCE))
        {
            return XProcPorts.ITERATION_SOURCE;
        }

        if (portNode.getNodeName().equals(Elements.XPATH_CONTEXT))
        {
            return XProcPorts.XPATH_CONTEXT;
        }

        throw new IllegalStateException(portNode.getNodeName().toString());
    }

    private Iterable<PortBinding> parsePortBindings(final XdmNode portNode)
    {
        return Iterables.transform(SaxonAxis.childElements(portNode, Elements.ELEMENTS_PORT_BINDINGS),
                new Function<XdmNode, PortBinding>()
                {
                    @Override
                    public PortBinding apply(final XdmNode node)
                    {
                        return parsePortBinding(node);
                    }
                });
    }

    private void unsupportedElement(final XdmNode node)
    {
        throw new IllegalStateException(node.getNodeName().toString());
    }

    private static Location getLocation(final XdmNode node)
    {
        // TODO cache locations per node (slower at parsing but less memory
        // used)
        return SaxonLocation.of(node);
    }

    private Step getDeclaredStep(final QName name)
    {
        final Step fromLocalLibrary = localLibrary.get(name);
        if (fromLocalLibrary != null)
        {
            return fromLocalLibrary;
        }

        final Step fromImportedLibrary = importedLibrary.get(name);
        if (fromImportedLibrary != null)
        {
            return fromImportedLibrary;
        }

        throw new UnsupportedOperationException(name.toString());
    }

    private Step parseDeclarePort(final XdmNode portNode, final Step step)
    {
        final String portName;
        if (portNode.getNodeName().equals(Elements.XPATH_CONTEXT))
        {
            portName = XProcPorts.XPATH_CONTEXT;
        }
        else
        {
            portName = portNode.getAttributeValue(Attributes.PORT);
        }
        final Port.Type type = getPortType(portNode);

        Port port = Port.newPort(step.getName(), portName, getLocation(portNode), type);
        port = port.setPrimary(portNode.getAttributeValue(Attributes.PRIMARY));
        port = port.setSequence(portNode.getAttributeValue(Attributes.SEQUENCE));
        port = port.setSelect(portNode.getAttributeValue(Attributes.SELECT));
        port = port.setPortBindings(parsePortBindings(portNode));
        LOG.trace("new port: {}", port);

        return step.declarePort(port);
    }

    private static Type getPortType(final XdmNode node)
    {
        if (Elements.ELEMENTS_INPUT_PORTS.contains(node.getNodeName()))
        {
            if ("parameter".equals(node.getAttributeValue(Attributes.KIND)))
            {
                return Type.PARAMETER;
            }
            else
            {
                return Type.INPUT;
            }
        }

        return Type.OUTPUT;
    }

    private PortBinding parsePortBinding(final XdmNode portBindingNode)
    {
        if (portBindingNode.getNodeName().equals(Elements.PIPE))
        {
            final String stepName = portBindingNode.getAttributeValue(Attributes.STEP);
            final String portName = portBindingNode.getAttributeValue(Attributes.PORT);
            return new PipePortBinding(PortReference.newReference(stepName, portName), getLocation(portBindingNode));
        }

        if (portBindingNode.getNodeName().equals(Elements.EMPTY))
        {
            return new EmptyPortBinding(getLocation(portBindingNode));
        }

        if (portBindingNode.getNodeName().equals(Elements.DOCUMENT))
        {
            final String href = portBindingNode.getAttributeValue(Attributes.HREF);
            return new DocumentPortBinding(href, getLocation(portBindingNode));
        }

        if (portBindingNode.getNodeName().equals(Elements.INLINE))
        {
            final XdmNode inlineNode = SaxonAxis.childElement(portBindingNode);
            return new InlinePortBinding(inlineNode, getLocation(portBindingNode));
        }

        throw new PipelineException("not supported: %s", portBindingNode.getNodeName());
    }

    private Step parseOption(final XdmNode node, final Step step)
    {
        LOG.trace("step = {}", step.getType());
        final QName name = new QName(node.getAttributeValue(Attributes.NAME), node);
        Variable option = Variable.newOption(name, getLocation(node));

        final String select = node.getAttributeValue(Attributes.SELECT);
        LOG.trace("name = {} ; select = {}", name, select);
        option = option.setSelect(select);

        final String required = node.getAttributeValue(Attributes.REQUIRED);
        LOG.trace("name = {} ; required = {}", name, required);
        if (required != null)
        {
            option = option.setRequired(Boolean.parseBoolean(required));
        }

        return step.declareVariable(option);
    }

    private Step parseWithParam(final XdmNode node, final Step step)
    {
        LOG.trace("step = {}", step.getType());
        final QName name = new QName(node.getAttributeValue(Attributes.NAME), node);
        final String select = node.getAttributeValue(Attributes.SELECT);
        LOG.trace("name = {} ; select = {}", name, select);
        return step.withParam(name, select, null, getLocation(node));
    }

    private Step parseWithOption(final XdmNode node, final Step step)
    {
        LOG.trace("step = {}", step.getType());
        final QName name = new QName(node.getAttributeValue(Attributes.NAME), node);
        final String select = node.getAttributeValue(Attributes.SELECT);
        LOG.trace("name = {} ; select = {}", name, select);
        return step.withOption(name, select);
    }

    private Step parseDeclareVariable(final XdmNode node, final Step step)
    {
        final QName name = new QName(node.getAttributeValue(Attributes.NAME), node);
        final String select = node.getAttributeValue(Attributes.SELECT);
        Variable variable = Variable.newVariable(name);
        variable = variable.setLocation(getLocation(node));
        variable = variable.setSelect(select);
        variable = variable.setRequired(true);
        final PortBinding portBinding = Iterables.getOnlyElement(parsePortBindings(node), null);
        variable = variable.setPortBinding(portBinding);

        return step.declareVariable(variable);
    }

    private void parseImports(final XdmNode node)
    {
        for (final XdmNode importNode : SaxonAxis.childElements(node, Elements.IMPORT))
        {
            parseImport(importNode);
        }
    }

    private void parseImport(final XdmNode node)
    {
        final String href = node.getAttributeValue(Attributes.HREF);
        assert href != null;
        LOG.trace("href = {}", href);
        final Source librarySource;
        try
        {
            librarySource = processor.getUnderlyingConfiguration().getURIResolver().resolve(href, source.getSystemId());
        }
        catch (final TransformerException e)
        {
            throw new PipelineException(e, "href = %s", href);
        }
        final String librarySystemId = librarySource.getSystemId();
        LOG.trace("librarySystemId = {} ; libraries = {}", librarySystemId, libraries);
        if (!libraries.contains(librarySystemId))
        {
            libraries.add(librarySystemId);
            final PipelineParser parser = new PipelineParser(processor, librarySource, importedLibrary, stepProcessors,
                    libraries);
            parser.parse();
            final Map<QName, Step> newLibrary = parser.getLibrary();
            LOG.trace("new steps = {}", newLibrary.keySet());
            localLibrary.putAll(newLibrary);
        }
    }

    private Collection<QName> getSupportedStepTypes()
    {
        // TODO improve performance
        final Collection<QName> types = Lists.newArrayList();
        types.addAll(localLibrary.keySet());
        types.addAll(importedLibrary.keySet());
        return types;
    }

    private Step parseInstanceStep(final XdmNode node)
    {
        final String name = getStepName(node);
        final QName type = node.getNodeName();
        LOG.trace("name = {} ; type = {}", name, type);

        final Step declaredStep = getDeclaredStep(type);
        Step step = declaredStep.setName(name).setLocation(getLocation(node)).setNode(node);
        LOG.trace("new instance step: {}", step);

        step = parseStepChildNodes(node, step);

        return step;
    }

    private String getStepName(final XdmNode node)
    {
        final String explicitName = node.getAttributeValue(Attributes.NAME);
        if (explicitName != null && explicitName.length() > 0)
        {
            return explicitName;
        }

        return getImplicitName(node);
    }

    private String getImplicitName(final XdmNode node)
    {
        return "!" + getImplicitName(rootNode, node);
    }

    private String getImplicitName(final XdmNode rootNode, final XdmNode node)
    {
        if (rootNode == node || node.getParent() == null)
        {
            return "1";
        }

        final int index = getNodeIndex(node);
        final XdmNode parentNode = node.getParent();

        return getImplicitName(rootNode, parentNode) + "." + Integer.toString(index);
    }

    private int getNodeIndex(final XdmNode node)
    {
        final XdmNode parentNode = node.getParent();
        if (parentNode == null)
        {
            return 1;
        }

        final List<XdmNode> childNodes = ImmutableList.copyOf(SaxonAxis.childElements(parentNode, getStepElements()));
        assert childNodes.contains(node) : node.getNodeName();
        return childNodes.indexOf(node) + 1;
    }

    private Collection<QName> getStepElements()
    {
        final Collection<QName> elements = Sets.newHashSet();
        elements.addAll(getSupportedStepTypes());
        elements.add(Elements.DECLARE_STEP);
        elements.add(Elements.LIBRARY);
        elements.add(Elements.PIPELINE);
        return elements;
    }

    public Map<QName, Step> getLibrary()
    {
        return ImmutableMap.copyOf(localLibrary);
    }

    public Step getPipeline()
    {
        return mainPipeline;
    }
}
