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
package org.trancecode.xproc;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import org.trancecode.collection.TcIterables;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.XProcXmlModel.Attributes;
import org.trancecode.xproc.XProcXmlModel.Elements;
import org.trancecode.xproc.api.PipelineException;
import org.trancecode.xproc.binding.DataPortBinding;
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
import org.trancecode.xproc.step.XProcSteps;
import org.trancecode.xproc.variable.Variable;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Herve Quiroz
 */
public final class PipelineParser
{
    private static final Logger LOG = Logger.getLogger(PipelineParser.class);

    private final URI baseUri;
    private final PipelineContext context;
    private final Source source;
    private final PipelineLibrary library;
    private final Map<QName, Step> localLibrary = Maps.newHashMap();

    private Step mainPipeline;
    private XdmNode rootNode;

    private final Map<URI, XdmNode> imports = Maps.newHashMap();
    private final Collection<XdmNode> declareStepNodes = Lists.newArrayList();

    public static PipelineLibrary parseLibrary(final PipelineContext context, final Source source)
    {
        LOG.trace("{@method} source = {}", source.getSystemId());
        final PipelineParser parser = new PipelineParser(context, source);
        parser.parse();
        return parser.getLibrary();
    }

    public static PipelineLibrary parseLibrary(final PipelineContext context, final Source source,
            final PipelineLibrary library)
    {
        LOG.trace("{@method} source = {}", source.getSystemId());
        final PipelineParser parser = new PipelineParser(context, source, library);
        parser.parse();
        return parser.getLibrary();
    }

    public static Step parsePipeline(final PipelineContext context, final Source source)
    {
        LOG.trace("{@method} source = {}", source.getSystemId());
        final PipelineParser parser = new PipelineParser(context, source);
        parser.parse();
        return parser.getPipeline();
    }

    public static Step parsePipeline(final PipelineContext context, final Source source, final PipelineLibrary library)
    {
        LOG.trace("{@method} source = {}", source.getSystemId());
        final PipelineParser parser = new PipelineParser(context, source, library);
        parser.parse();
        return parser.getPipeline();
    }

    private PipelineParser(final PipelineContext context, final Source source)
    {
        this(context, source, context.getPipelineLibrary());
    }

    private PipelineParser(final PipelineContext context, final Source source, final PipelineLibrary library)
    {
        this.context = Preconditions.checkNotNull(context);
        this.source = Preconditions.checkNotNull(source);
        this.library = Preconditions.checkNotNull(library);
        baseUri = URI.create(source.getSystemId());
    }

    private void parse()
    {
        try
        {
            final DocumentBuilder documentBuilder = context.getProcessor().newDocumentBuilder();
            documentBuilder.setLineNumbering(true);
            final XdmNode pipelineDocument = documentBuilder.build(source);
            rootNode = SaxonAxis.childElement(pipelineDocument, Elements.ELEMENTS_ROOT);
            Preconditions.checkState(rootNode != null);

            parseImports(rootNode);
            parseDeclareSteps();

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
                throw new IllegalStateException("invalid root node: " + rootNode.getNodeName());
            }
        }
        catch (final SaxonApiException e)
        {
            throw new PipelineException(e);
        }
    }

    private XdmNode getRootElement(final Source source)
    {
        try
        {
            final DocumentBuilder documentBuilder = context.getProcessor().newDocumentBuilder();
            documentBuilder.setLineNumbering(true);
            final XdmNode pipelineDocument = documentBuilder.build(source);
            return SaxonAxis.childElement(pipelineDocument, Elements.ELEMENTS_ROOT);
        }
        catch (final SaxonApiException e)
        {
            throw new PipelineException(e);
        }
    }

    private void declareStep(final Step step)
    {
        if (step.getType() != null)
        {
            localLibrary.put(step.getType(), step);
        }
    }

    private void parseDeclareSteps()
    {
        LOG.trace("{@method}");
        for (final Entry<URI, XdmNode> importEntry : imports.entrySet())
        {
            LOG.trace("  parsing imported library from {}", importEntry.getKey());
            final XdmNode importRootNode = importEntry.getValue();
            if (Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE.contains(importRootNode.getNodeName()))
            {
                mainPipeline = parseDeclareStep(importRootNode);
            }
            else if (importRootNode.getNodeName().equals(Elements.LIBRARY))
            {
                parseDeclareSteps(importRootNode);
            }
            else
            {
                unsupportedElement(importRootNode);
            }
        }
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
        if (context.getStepProcessors().containsKey(type))
        {
            final StepProcessor stepProcessor = context.getStepProcessor(type);
            LOG.trace("stepProcessor = {}", stepProcessor);
            step = Step.newStep(stepNode, type, stepProcessor, false);
        }
        else
        {
            final String name = getStepName(stepNode);
            step = PipelineStepProcessor.newPipeline(type);
            step = step.setName(name);
            if (Elements.PIPELINE.equals(stepNode.getNodeName()))
            {
                step = PipelineStepProcessor.addImplicitPorts(step);
            }
        }

        step = step.setLocation(getLocation(stepNode));

        step = parseStepChildNodes(stepNode, step);

        step.check();

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
            LOG.trace("{@method} element = {} ; step = {}", node.getNodeName(), step);

            final String useWhen = TcIterables.getFirstNonNull(node.getAttributeValue(Attributes.USE_WHEN),
                    node.getAttributeValue(Attributes.P_USE_WHEN));
            if (useWhen != null)
            {
                LOG.trace("  use-when = {}", useWhen);
                try
                {
                    final XPathCompiler xpathCompiler = context.getProcessor().newXPathCompiler();
                    final XPathSelector selector = xpathCompiler.compile(useWhen).load();
                    final XdmValue result = selector.evaluate();
                    if (!Saxon.isTrue(result))
                    {
                        return step;
                    }
                }
                catch (final SaxonApiException e)
                {
                    throw new PipelineException(e, "error while evaluating XPath query: %s" + useWhen);
                }
            }

            if (node.getNodeName().equals(Elements.IMPORT))
            {
                parseImport(node);
                return step;
            }

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

            if (Elements.ELEMENTS_IGNORED.contains(node.getNodeName()))
            {
                return step;
            }

            if (node.getNodeName().equals(Elements.LOG))
            {
                return parseLog(node, step);
            }

            // try and declare the missing step
            final QName childStepType = node.getNodeName();
            LOG.trace("  step {} is missing from the current library ; trying to declare it", childStepType);
            for (final XdmNode declareStepNode : declareStepNodes)
            {
                final QName declareStepType = Saxon.getAttributeAsQName(declareStepNode, Attributes.TYPE);
                LOG.trace("    declared step: {}", declareStepType);
                if (childStepType.equals(declareStepType))
                {
                    parseDeclareStep(declareStepNode);
                    return step.addChildStep(parseInstanceStep(node));
                }
            }

            throw XProcExceptions.xs0044(node);
        }
        else if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE)
        {
            LOG.trace("{} = {}", node.getNodeName(), node.getStringValue());
            final QName name = node.getNodeName();
            final String value = node.getStringValue();

            // TODO: it's not very good to test step's type here !
            if (name.getNamespaceURI().isEmpty()
                    && !name.equals(Attributes.NAME)
                    && !name.equals(Attributes.TYPE)
                    && !name.equals(Attributes.USE_WHEN)
                    && (!name.equals(Attributes.VERSION) || XProcSteps.XSLT.equals(step.getType()) || XProcSteps.HASH
                            .equals(step.getType())))
            {
                if (!step.hasOptionDeclared(name))
                {
                    throw XProcExceptions.xs0031(getLocation(node), name, step.getType());
                }
                return step.withOptionValue(name, value, node);
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

    private Step parseLog(final XdmNode logNode, final Step step)
    {
        final String port = logNode.getAttributeValue(Attributes.PORT);
        final String href = logNode.getAttributeValue(Attributes.HREF);
        LOG.trace("{@method} port = {} ; href = {}", port, href);
        if (!step.hasPortDeclared(port) || step.hasLogDeclaredForPort(port))
        {
            throw XProcExceptions.xs0026(getLocation(logNode), step, port);
        }
        return step.addLog(port, href);
    }

    private Step parseWithPort(final XdmNode portNode, final Step step)
    {
        final String portName = getPortName(portNode);
        LOG.trace("{@method} step = {} ; port = {}", step, portName);
        final Port port = step.getPort(portName);
        assert port.isParameter() || port.getType().equals(getPortType(portNode)) : "port = " + port.getType()
                + " ; with-port = " + getPortType(portNode);

        final String select = portNode.getAttributeValue(Attributes.SELECT);
        LOG.trace("select = {}", select);
        final String sequence = portNode.getAttributeValue(Attributes.SEQUENCE);
        LOG.trace("sequence = {}", sequence);

        final Port configuredPort = port.setSelect(select).setSequence(sequence)
                .setPortBindings(parsePortBindings(portNode));

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

        if (portNode.getNodeName().equals(Elements.VIEWPORT_SOURCE))
        {
            return XProcPorts.VIEWPORT_SOURCE;
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

        return library.newStep(name);
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

        if (portBindingNode.getNodeName().equals(Elements.DATA))
        {
            final String href = portBindingNode.getAttributeValue(Attributes.HREF);
            final String wrapper = portBindingNode.getAttributeValue(XProcOptions.WRAPPER);
            final String wrapperPrefix = portBindingNode.getAttributeValue(XProcOptions.WRAPPER_PREFIX);
            final String wrapperNamespace = portBindingNode.getAttributeValue(XProcOptions.WRAPPER_NAMESPACE);
            final String contentType = portBindingNode.getAttributeValue(XProcOptions.CONTENT_TYPE);
            return new DataPortBinding(href, wrapper, wrapperPrefix, wrapperNamespace, contentType, portBindingNode);
        }

        if (portBindingNode.getNodeName().equals(Elements.DOCUMENT))
        {
            final String href = portBindingNode.getAttributeValue(Attributes.HREF);
            return new DocumentPortBinding(href, getLocation(portBindingNode));
        }

        if (portBindingNode.getNodeName().equals(Elements.INLINE))
        {
            final XdmNode inlineDocument = Saxon.asDocumentNode(context.getProcessor(),
                    SaxonAxis.childNodesNoAttributes(portBindingNode));
            return new InlinePortBinding(inlineDocument, getLocation(portBindingNode));
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

        option = option.setNode(node);

        return step.declareVariable(option);
    }

    private Step parseWithParam(final XdmNode node, final Step step)
    {
        LOG.trace("step = {}", step.getType());
        final QName name = new QName(node.getAttributeValue(Attributes.NAME));
        final String select = node.getAttributeValue(Attributes.SELECT);
        LOG.trace("name = {} ; select = {}", name, select);
        return step.withParam(name, select, null, getLocation(node), node,
                Iterables.getOnlyElement(parsePortBindings(node), null));
    }

    private Step parseWithOption(final XdmNode node, final Step step)
    {
        LOG.trace("step = {}", step.getType());
        final QName name = new QName(node.getAttributeValue(Attributes.NAME));
        final String select = node.getAttributeValue(Attributes.SELECT);
        LOG.trace("name = {} ; select = {}", name, select);
        return step.withOption(name, select, node, Iterables.getOnlyElement(parsePortBindings(node), null));
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
        variable = variable.setNode(node);

        return step.declareVariable(variable);
    }

    private void parseImports(final XdmNode node)
    {
        for (final XdmNode importNode : SaxonAxis.childElements(node, Elements.IMPORT))
        {
            parseImport(importNode);
        }

        for (final XdmNode declareStepNode : SaxonAxis.childElements(node, Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE))
        {
            parseImports(declareStepNode);
        }

        if (Elements.ELEMENTS_DECLARE_STEP_OR_PIPELINE.contains(node.getNodeName()))
        {
            declareStepNodes.add(node);
        }
    }

    private void parseImport(final XdmNode node)
    {
        final String href = node.getAttributeValue(Attributes.HREF);
        assert href != null;
        LOG.trace("{@method} href = {}", href);
        final URI libraryUri = baseUri.resolve(href);
        LOG.trace("libraryUri = {} ; libraries = {keys}", libraryUri, imports);
        if (!imports.containsKey(libraryUri))
        {
            final Source librarySource;
            try
            {
                librarySource = context.getProcessor().getUnderlyingConfiguration().getURIResolver()
                        .resolve(href, node.getBaseURI().toString());
            }
            catch (final TransformerException e)
            {
                throw XProcExceptions.xs0052(SaxonLocation.of(node), libraryUri);
            }

            final XdmNode importedRootNode = getRootElement(librarySource);
            imports.put(libraryUri, importedRootNode);
            parseImports(importedRootNode);
        }
        else
        {
            LOG.trace("ignoring import statement (library already imported): {}", libraryUri);
        }
    }

    private Collection<QName> getSupportedStepTypes()
    {
        return Sets.union(library.getStepTypes(), localLibrary.keySet());
    }

    private Step parseInstanceStep(final XdmNode node)
    {
        final String name = getStepName(node);
        final QName type = node.getNodeName();
        LOG.trace("name = {} ; type = {}", name, type);

        final Step declaredStep = getDeclaredStep(type);
        Step step = declaredStep.setName(name).setLocation(getLocation(node)).setNode(node);
        LOG.trace("new instance step: {}", step);
        LOG.trace("step processor: {}", step.getStepProcessor());

        step = parseStepChildNodes(node, step);

        if (step.getType().equals(XProcSteps.FOR_EACH) && Iterables.isEmpty(step.getOutputPorts()))
        {
            final Port port = Port.newOutputPort(step.getName(), XProcPorts.RESULT, getLocation(node))
                    .setSequence(true);
            LOG.trace("  add implicit output port: {}", port);
            step = step.declarePort(port);
        }

        if (step.getType().equals(XProcSteps.VIEWPORT) && Iterables.isEmpty(step.getOutputPorts()))
        {
            final Port port = Port.newOutputPort(step.getName(), XProcPorts.RESULT, getLocation(node)).setSequence(
                    false);
            LOG.trace("  add implicit output port: {}", port);
            step = step.declarePort(port);
        }

        if (step.getType().equals(XProcSteps.GROUP) && Iterables.isEmpty(step.getOutputPorts()))
        {
            final Port port = Port.newOutputPort(step.getName(), XProcPorts.RESULT, getLocation(node));
            LOG.trace("  add implicit output port: {}", port);
            step = step.declarePort(port);
        }

        if (step.getType().equals(XProcSteps.CATCH) && Iterables.isEmpty(step.getOutputPorts()))
        {
            final Port port = Port.newOutputPort(step.getName(), XProcPorts.RESULT, getLocation(node))
                    .setSequence(true);
            LOG.trace("  add implicit output port: {}", port);
            step = step.declarePort(port);
        }

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

    private PipelineLibrary getLibrary()
    {
        final Set<URI> uris = ImmutableSet.of();
        return new PipelineLibrary(baseUri, localLibrary, uris, mainPipeline).importLibrary(library);
    }

    private Step getPipeline()
    {
        return mainPipeline;
    }
}
