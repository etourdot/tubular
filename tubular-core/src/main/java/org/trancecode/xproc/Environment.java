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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.apache.commons.lang.StringUtils;
import org.trancecode.api.ReturnsNullable;
import org.trancecode.collection.TcMaps;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xml.saxon.SaxonNamespaces;
import org.trancecode.xproc.api.PipelineException;
import org.trancecode.xproc.api.XProcException;
import org.trancecode.xproc.binding.PortBinding;
import org.trancecode.xproc.port.EnvironmentPort;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.PortFunctions;
import org.trancecode.xproc.port.PortReference;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.step.Step;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public final class Environment
{
    private static final Logger LOG = Logger.getLogger(Environment.class);

    private static final QName ATTRIBUTE_NAME = new QName("name");
    private static final QName ATTRIBUTE_NAMESPACE = new QName("namespace");
    private static final QName ATTRIBUTE_VALUE = new QName("value");
    private static final QName ELEMENT_PARAM = XProcXmlModel.xprocStepNamespace().newSaxonQName("param");
    private static final QName ELEMENT_RESULT = XProcXmlModel.xprocStepNamespace().newSaxonQName("result");

    private static final ThreadLocal<Environment> CURRENT_ENVIRONMENT = new ThreadLocal<Environment>();
    private static final ThreadLocal<XdmNode> CURRENT_XPATH_CONTEXT = new ThreadLocal<XdmNode>();
    private static final ThreadLocal<XdmNode> CURRENT_NAMESPACE_CONTEXT = new ThreadLocal<XdmNode>();

    private final EnvironmentPort defaultReadablePort;
    private final Map<QName, String> inheritedVariables;
    private final Map<QName, String> localVariables;
    private final PipelineContext configuration;
    private final Map<PortReference, EnvironmentPort> ports;
    private final Step pipeline;
    private final EnvironmentPort defaultParametersPort;
    private final EnvironmentPort xpathContextPort;

    public static void setCurrentNamespaceContext(final XdmNode node)
    {
        CURRENT_NAMESPACE_CONTEXT.set(node);
    }

    public static XdmNode getCurrentNamespaceContext()
    {
        return CURRENT_NAMESPACE_CONTEXT.get();
    }

    public static void setCurrentXPathContext(final XdmNode node)
    {
        CURRENT_XPATH_CONTEXT.set(node);
    }

    public static XdmNode getCurrentXPathContext()
    {
        return CURRENT_XPATH_CONTEXT.get();
    }

    public static void setCurrentEnvironment(final Environment environment)
    {
        CURRENT_ENVIRONMENT.set(environment);
    }

    public void setCurrentEnvironment()
    {
        setCurrentEnvironment(this);
    }

    public static Environment getCurrentEnvironment()
    {
        return CURRENT_ENVIRONMENT.get();
    }

    private static Map<PortReference, EnvironmentPort> getPortsMap(final Iterable<EnvironmentPort> ports)
    {
        return Maps.uniqueIndex(ports, PortFunctions.getPortReference());
    }

    public static Environment newEnvironment(final Step pipeline, final PipelineContext configuration)
    {
        final Map<QName, String> variables = ImmutableMap.of();
        final Iterable<EnvironmentPort> ports = ImmutableList.of();
        return new Environment(pipeline, configuration, ports, null, null, null, variables, variables);
    }

    private Environment(final Step pipeline, final PipelineContext configuration,
            final Iterable<EnvironmentPort> ports, final EnvironmentPort defaultReadablePort,
            final EnvironmentPort defaultParametersPort, final EnvironmentPort xpathContextPort,
            final Map<QName, String> inheritedVariables, final Map<QName, String> localVariables)
    {
        this(pipeline, configuration, getPortsMap(ports), defaultReadablePort, defaultParametersPort, xpathContextPort,
                inheritedVariables, localVariables);
    }

    private Environment(final Step pipeline, final PipelineContext configuration,
            final Map<PortReference, EnvironmentPort> ports, final EnvironmentPort defaultReadablePort,
            final EnvironmentPort defaultParametersPort, final EnvironmentPort xpathContextPort,
            final Map<QName, String> inheritedVariables, final Map<QName, String> localVariables)
    {
        this.pipeline = pipeline;
        this.configuration = configuration;
        this.ports = ImmutableMap.copyOf(ports);
        this.defaultReadablePort = defaultReadablePort;
        this.defaultParametersPort = defaultParametersPort;
        this.xpathContextPort = xpathContextPort;
        this.inheritedVariables = ImmutableMap.copyOf(inheritedVariables);
        this.localVariables = ImmutableMap.copyOf(localVariables);
    }

    private Environment setupStepEnvironment(final Step step, final boolean evaluteVariables)
    {
        LOG.trace("{@method} step = {}", step.getName());

        Environment environment = setupInputPorts(step);
        environment = environment.setPrimaryInputPortAsDefaultReadablePort(step);
        environment = environment.setXPathContextPort(step);
        environment = environment.setDefaultParametersPort(step);
        if (evaluteVariables)
        {
            environment = environment.setupVariables(step);
        }
        environment = environment.setDefaultParametersPort(step);

        return environment;
    }

    private Environment setupInputPorts(final Step step)
    {
        LOG.trace("{@method} step = {}", step.getName());

        final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();

        for (final Port port : step.getInputPorts())
        {
            EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(port, this);
            if (port.getPortName().equals(XProcPorts.XPATH_CONTEXT) && port.getPortBindings().isEmpty()
                    && getXPathContextPort() != null)
            {
                LOG.trace("  {} is XPath context port", environmentPort);
                environmentPort = environmentPort.pipe(getXPathContextPort());
            }
            if (port.isParameter() && defaultParametersPort != null)
            {
                environmentPort = environmentPort.pipe(defaultParametersPort);
            }
            newPorts.put(port.getPortReference(), environmentPort);
        }

        for (final Port port : step.getOutputPorts())
        {
            if (port.getPortBindings().isEmpty())
            {
                newPorts.put(port.getPortReference(), EnvironmentPort.newEnvironmentPort(port, this));
            }
        }

        return addPorts(newPorts);
    }

    public Environment setupOutputPorts(final Step step)
    {
        LOG.trace("{@method} step = {}", step.getName());

        return setupOutputPorts(step, this);
    }

    public Environment setupOutputPorts(final Step step, final Environment sourceEnvironment)
    {
        LOG.trace("{@method} step = {}", step.getName());

        final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap();

        for (final Port port : step.getOutputPorts())
        {
            if (!ports.containsKey(port.getPortReference()))
            {
                newPorts.put(port.getPortReference(), EnvironmentPort.newEnvironmentPort(port, sourceEnvironment));
            }
        }

        Environment result = addPorts(newPorts);
        result = result.setPrimaryOutputPortAsDefaultReadablePort(step, sourceEnvironment);
        result = result.setDefaultReadablePortAsXPathContextPort();
        return result;
    }

    public Environment setDefaultReadablePortAsXPathContextPort()
    {
        final EnvironmentPort port = getDefaultReadablePort();
        LOG.trace("{@method} port = {}", port);
        return setXPathContextPort(port);
    }

    private Environment setPrimaryInputPortAsDefaultReadablePort(final Step step)
    {
        LOG.trace("{@method} step = {} ; type = {}", step.getName(), step.getType());

        final Port primaryInputPort = step.getPrimaryInputPort();
        LOG.trace("primaryInputPort = {}", primaryInputPort);

        if (primaryInputPort == null)
        {
            return this;
        }

        LOG.trace("new default readable port = {}", primaryInputPort);

        // if port is empty then pipe to existing default readable port
        final EnvironmentPort environmentPort = getEnvironmentPort(primaryInputPort);
        final EnvironmentPort nonEmptyEnvironmentPort;
        if (Iterables.isEmpty(environmentPort.portBindings()) && getDefaultReadablePort() != null)
        {
            nonEmptyEnvironmentPort = environmentPort.pipe(getDefaultReadablePort());
        }
        else
        {
            nonEmptyEnvironmentPort = environmentPort;
        }

        return addPorts(nonEmptyEnvironmentPort).setDefaultReadablePort(nonEmptyEnvironmentPort);
    }

    public Environment setPrimaryOutputPortAsDefaultReadablePort(final Step step, final Environment sourceEnvironment)
    {
        LOG.trace("{@method} step = {}", step.getName());

        final Port primaryOutputPort = step.getPrimaryOutputPort();

        if (primaryOutputPort == null)
        {
            return this;
        }

        LOG.trace("new default readable port = {}", primaryOutputPort);

        final EnvironmentPort environmentPort = getEnvironmentPort(primaryOutputPort);
        final EnvironmentPort nonEmptyEnvironmentPort;
        if (Iterables.isEmpty(environmentPort.portBindings()))
        {
            nonEmptyEnvironmentPort = environmentPort.pipe(sourceEnvironment.getDefaultReadablePort());
        }
        else
        {
            nonEmptyEnvironmentPort = environmentPort;
        }

        return addPorts(nonEmptyEnvironmentPort).setDefaultReadablePort(nonEmptyEnvironmentPort);
    }

    private Environment setXPathContextPort(final Step step)
    {
        LOG.trace("{@method} step = {}", step.getName());

        final Port xpathContextPort = step.getXPathContextPort();
        if (xpathContextPort == null)
        {
            return this;
        }

        return setXPathContextPort(getEnvironmentPort(xpathContextPort));
    }

    public Environment setupVariables(final Step step)
    {
        LOG.trace("{@method} step = {}", step.getName());
        final Iterable<Variable> allDeclaredVariables = Iterables.concat(step.getVariables().values(), step
                .getParameters().values());
        LOG.trace("  variables = {}", allDeclaredVariables);

        final Map<QName, String> allVariables = Maps.newHashMap(inheritedVariables);
        allVariables.putAll(localVariables);

        final Map<QName, String> newLocalVariables = Maps.newHashMap(localVariables);
        final List<XdmNode> newParameterNodes = Lists.newArrayListWithCapacity(step.getParameters().size());

        for (final Variable variable : allDeclaredVariables)
        {
            LOG.trace("variable = {}", variable);
            final String value;
            if (variable.getValue() != null)
            {
                value = variable.getValue();
            }
            else if (variable.getSelect() == null)
            {
                if (variable.isRequired())
                {
                    throw XProcExceptions.xs0018(variable);
                }

                value = null;
            }
            else
            {
                final PortBinding xpathPortBinding = variable.getPortBinding();
                final XdmNode xpathContextNode;
                if (xpathPortBinding != null)
                {
                    try
                    {
                        xpathContextNode = Iterables.getOnlyElement(xpathPortBinding.newEnvironmentPortBinding(this)
                                .readNodes(), null);
                    }
                    catch (final NoSuchElementException e)
                    {
                        // TODO XProc error?
                        throw new IllegalStateException("error while evaluating " + variable.getName(), e);
                    }
                }
                else
                {
                    xpathContextNode = getXPathContextNode(variable);
                }

                final XdmValue result = evaluateXPath(variable.getSelect(), getPipelineContext().getProcessor(),
                        xpathContextNode, variable.getNode(), allVariables, variable.getLocation());
                final XdmItem resultNode = Iterables.getOnlyElement(result);

                value = resultNode.getStringValue();
            }

            LOG.trace("{} = {}", variable.getName(), value);

            if (value != null)
            {
                if (variable.isParameter())
                {
                    if (getDefaultParametersPort() == null)
                    {
                        throw XProcExceptions.xs0034(variable.getLocation(), step, variable.getName());
                    }
                    final XdmNode parameterNode = newParameterElement(variable.getName(), value);
                    newParameterNodes.add(parameterNode);
                }
                else
                {
                    allVariables.put(variable.getName(), value);
                    newLocalVariables.put(variable.getName(), value);
                }
            }
        }

        final EnvironmentPort parametersPort = getDefaultParametersPort();
        final Environment resultEnvironment;
        if (newParameterNodes.isEmpty())
        {
            resultEnvironment = this;
        }
        else
        {
            assert parametersPort != null : step;
            final EnvironmentPort newParametersPort = parametersPort.writeNodes(newParameterNodes, true);
            resultEnvironment = addPorts(newParametersPort);
        }

        return resultEnvironment.setLocalVariables(newLocalVariables);
    }

    private static XdmValue evaluateXPath(final String select, final Processor processor,
            final XdmNode xpathContextNode, final XdmNode namespaceContextNode, final Map<QName, String> variables,
            final Location location)
    {
        LOG.trace("{@method} select = {} ; variables = {}", select, variables);

        try
        {
            final XPathCompiler xpathCompiler = processor.newXPathCompiler();
            for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
            {
                if (variableEntry.getValue() != null)
                {
                    xpathCompiler.declareVariable(variableEntry.getKey());
                }
            }

            for (final Entry<String, String> namespace : SaxonNamespaces.namespaceSequence(namespaceContextNode))
            {
                xpathCompiler.declareNamespace(namespace.getKey(), namespace.getValue());
            }

            setCurrentNamespaceContext(namespaceContextNode);

            final XPathSelector selector = xpathCompiler.compile(select).load();
            if (xpathContextNode != null)
            {
                LOG.trace("xpathContextNode = {}", xpathContextNode);
                selector.setContextItem(processor.newDocumentBuilder().build(xpathContextNode.asSource()));
                setCurrentXPathContext(xpathContextNode);
            }

            for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
            {
                if (variableEntry.getValue() != null)
                {
                    LOG.trace("  {} = {}", variableEntry.getKey(), variableEntry.getValue());
                    selector.setVariable(variableEntry.getKey(), new XdmAtomicValue(variableEntry.getValue()));
                }
            }

            final XdmValue result = selector.evaluate();
            LOG.trace("result = {}", result);
            return result;
        }
        catch (final SaxonApiException e)
        {
            final XProcException xprocException = XProcExceptions.xd0023(location, select, e.getMessage());
            xprocException.initCause(e);
            throw xprocException;
        }
    }

    public Environment newFollowingStepEnvironment(final Step step)
    {
        return newFollowingStepEnvironment(step, true);
    }

    public Environment newFollowingStepEnvironment(final Step step, final boolean evaluateVariables)
    {
        LOG.trace("{@method} step = {}", step.getName());

        return newFollowingStepEnvironment().setupStepEnvironment(step, evaluateVariables).setupStepAlias(step);
    }

    public Environment newFollowingStepEnvironment()
    {
        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, localVariables);
    }

    private Environment setupStepAlias(final Step step)
    {
        Environment environment = this;
        final String internalStepName = step.getInternalName();
        if (internalStepName != null)
        {
            LOG.trace("step alias {} -> {}", step.getName(), internalStepName);
            for (final Port port : step.getInputPorts())
            {
                final Port internalPort = port.setStepName(internalStepName).pipe(port);
                LOG.trace("{} -> {}", internalPort, port);
                final EnvironmentPort environmentPort = EnvironmentPort.newEnvironmentPort(internalPort, environment);
                environment = environment.addPorts(environmentPort);
            }
        }
        return environment;
    }

    public Environment newChildStepEnvironment()
    {
        final Map<QName, String> variables = ImmutableMap.of();
        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, TcMaps.merge(inheritedVariables, localVariables), variables);
    }

    public Environment setLocalVariables(final Map<QName, String> localVariables)
    {
        assert localVariables != null;

        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, TcMaps.merge(this.localVariables, localVariables));
    }

    public void setLocalVariable(final QName name, final String value)
    {
        localVariables.put(name, value);
    }

    public EnvironmentPort getDefaultReadablePort()
    {
        return defaultReadablePort;
    }

    public EnvironmentPort getXPathContextPort()
    {
        return xpathContextPort;
    }

    public Environment setXPathContextPort(final EnvironmentPort xpathContextPort)
    {
        LOG.trace("{@method} port = {}", xpathContextPort);
        assert xpathContextPort == null || ports.containsValue(xpathContextPort);

        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, localVariables);
    }

    public PipelineContext getPipelineContext()
    {
        return configuration;
    }

    public String getVariable(final QName name, final String defaultValue)
    {
        assert name != null;
        LOG.trace("{@method} name = {} ; defaultValue = {}", name, defaultValue);

        final String value = getVariable(name);
        if (value != null)
        {
            return value;
        }

        return defaultValue;
    }

    public String getVariable(final QName name)
    {
        assert name != null;
        LOG.trace("{@method} name = {}", name);

        final String localValue = localVariables.get(name);
        if (localValue != null)
        {
            return localValue;
        }

        return inheritedVariables.get(name);
    }

    public Map<QName, String> getLocalVariables()
    {
        return localVariables;
    }

    public Map<QName, String> getInheritedVariables()
    {
        return inheritedVariables;
    }

    public Environment setDefaultReadablePort(final EnvironmentPort defaultReadablePort)
    {
        assert defaultReadablePort == null || ports.containsValue(defaultReadablePort) : defaultReadablePort
                .getPortReference() + " ; " + ports.keySet();
        LOG.trace("{@method} defaultReadablePort = {}", defaultReadablePort);

        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, localVariables);
    }

    public Environment setDefaultReadablePort(final PortReference portReference)
    {
        return setDefaultReadablePort(getPort(portReference));
    }

    public Map<PortReference, EnvironmentPort> getPorts()
    {
        return ports;
    }

    public EnvironmentPort getEnvironmentPort(final Port port)
    {
        return getEnvironmentPort(port.getPortReference());
    }

    public EnvironmentPort getEnvironmentPort(final PortReference portReference)
    {
        final EnvironmentPort port = ports.get(portReference);
        Preconditions.checkArgument(port != null, "no such port: %s\navailable ports: %s", portReference,
                ports.keySet());
        return port;
    }

    public Environment addPorts(final EnvironmentPort... ports)
    {
        return addPorts(ImmutableList.copyOf(ports));
    }

    public Environment addPorts(final Iterable<EnvironmentPort> ports)
    {
        assert ports != null;
        LOG.trace("{@method} ports = {}", ports);

        final Map<PortReference, EnvironmentPort> newPorts = Maps.newHashMap(this.ports);
        newPorts.putAll(getPortsMap(ports));

        return new Environment(pipeline, configuration, newPorts, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, localVariables);
    }

    public Environment addPorts(final Map<PortReference, EnvironmentPort> ports)
    {
        assert ports != null;
        LOG.trace("{@method} ports = {}", ports);

        return new Environment(pipeline, configuration, TcMaps.merge(this.ports, ports), defaultReadablePort,
                defaultParametersPort, xpathContextPort, inheritedVariables, localVariables);
    }

    public Step getPipeline()
    {
        return pipeline;
    }

    public URI getBaseUri()
    {
        return URI.create(pipeline.getLocation().getSystemId());
    }

    public EnvironmentPort getDefaultParametersPort()
    {
        return defaultParametersPort;
    }

    private Environment setDefaultParametersPort(final Step step)
    {
        LOG.trace("{@method} step = {}", step);
        final Port port = step.getPrimaryParameterPort();
        LOG.trace("  port = {}", port);
        if (port != null)
        {
            final EnvironmentPort environmentPort = getEnvironmentPort(port);
            assert environmentPort != null;
            return setDefaultParametersPort(environmentPort);
        }

        return this;
    }

    public Environment setDefaultParametersPort(final EnvironmentPort defaultParametersPort)
    {
        assert defaultParametersPort == null || ports.containsValue(defaultParametersPort) : String.format(
                "port = %s ; defaultParametersPort = %s", ports, defaultParametersPort);
        LOG.trace("{@method} defaultParametersPort = {}", defaultParametersPort);

        return new Environment(pipeline, configuration, ports, defaultReadablePort, defaultParametersPort,
                xpathContextPort, inheritedVariables, localVariables);
    }

    @ReturnsNullable
    public XdmNode getXPathContextNode()
    {
        LOG.trace("{@method}");

        // TODO cache

        final EnvironmentPort xpathContextPort = getXPathContextPort();
        LOG.trace("  xpathContextPort = {}", xpathContextPort);
        if (xpathContextPort != null)
        {
            final Iterator<XdmNode> contextNodes = xpathContextPort.readNodes().iterator();
            if (contextNodes.hasNext())
            {
                final XdmNode contextNode = contextNodes.next();
                if (xpathContextPort.getDeclaredPort().getPortName().equals(XProcPorts.XPATH_CONTEXT))
                {
                    // TODO XProc error
                    assert !contextNodes.hasNext() : xpathContextPort.readNodes();
                }

                return Saxon.asDocumentNode(contextNode, configuration.getProcessor());
            }
        }

        return Saxon.getEmptyDocument(configuration.getProcessor());
    }

    @ReturnsNullable
    public XdmNode getXPathContextNode(final Variable variable)
    {
        final EnvironmentPort xpathContextPort = getXPathContextPort();
        if (xpathContextPort != null)
        {
            final Iterable<XdmNode> nodes = xpathContextPort.readNodes();
            if (Iterables.size(nodes) > 1 && variable.isVariable())
            {
                throw XProcExceptions.xd0008(SaxonLocation.of(variable.getNode()));
            }
            else if (!Iterables.isEmpty(nodes))
            {
                return Iterables.getFirst(nodes, null);
            }
        }
        return Saxon.getEmptyDocument(configuration.getProcessor());
    }

    public XdmValue evaluateXPath(final String select)
    {
        assert select != null;
        LOG.trace("{@method} select = {}", select);

        final XdmNode xpathContextNode = getXPathContextNode();
        assert xpathContextNode != null;
        LOG.trace("xpathContextNode = {}", xpathContextNode);

        return evaluateXPath(select, xpathContextNode);
    }

    public XdmValue evaluateXPath(final String select, final XdmNode xpathContextNode)
    {
        return evaluateXPath(select, xpathContextNode, null);
    }

    public XdmValue evaluateXPath(final String select, final XdmNode xpathContextNode,
            final Map<QName, String> additionalParameters)
    {
        assert select != null;
        LOG.trace("{@method} select = {}", select);

        // TODO slow
        Map<QName, String> variables = TcMaps.merge(inheritedVariables, localVariables);

        if (additionalParameters != null)
        {
            variables = TcMaps.merge(variables, additionalParameters);
        }

        try
        {
            final XPathCompiler xpathCompiler = configuration.getProcessor().newXPathCompiler();
            final String pipelineSystemId = getPipeline().getLocation().getSystemId();
            if (pipelineSystemId != null)
            {
                xpathCompiler.setBaseURI(URI.create(pipelineSystemId));
            }
            for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
            {
                if (variableEntry.getValue() != null)
                {
                    xpathCompiler.declareVariable(variableEntry.getKey());
                    LOG.trace("declare var for XPath context :{}", variableEntry.getKey());
                }
            }

            xpathCompiler.declareNamespace(XProcXmlModel.xprocNamespace().prefix(), XProcXmlModel.xprocNamespace()
                    .uri());
            xpathCompiler.declareNamespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel
                    .xprocStepNamespace().uri());

            LOG.trace("decl prfx:{}", XProcXmlModel.xprocStepNamespace().prefix());

            final XPathSelector selector = xpathCompiler.compile(select).load();
            setCurrentXPathContext(xpathContextNode);
            if (xpathContextNode != null)
            {
                selector.setContextItem(xpathContextNode);
            }

            for (final Map.Entry<QName, String> variableEntry : variables.entrySet())
            {
                if (variableEntry.getValue() != null)
                {
                    selector.setVariable(variableEntry.getKey(),
                            Saxon.getUntypedXdmItem(variableEntry.getValue(), configuration.getProcessor()));
                }
            }

            return selector.evaluate();
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("error while evaluating XPath query: " + select, e);
        }
    }

    private EnvironmentPort getPort(final PortReference portReference)
    {
        assert ports.containsKey(portReference) : "port = " + portReference + " ; ports = " + ports.keySet();
        return ports.get(portReference);
    }

    public Environment writeNodes(final PortReference portReference, final XdmNode... nodes)
    {
        return writeNodes(portReference, ImmutableList.copyOf(nodes));
    }

    public Environment writeNodes(final PortReference portReference, final Iterable<XdmNode> nodes)
    {
        LOG.trace("{@method} port = {}", portReference);

        return addPorts(getPort(portReference).writeNodes(nodes));
    }

    public Iterable<XdmNode> readNodes(final PortReference portReference)
    {
        LOG.trace("{@method} port = {}", portReference);
        final Iterable<XdmNode> nodes = getPort(portReference).readNodes();
        LOG.trace("nodes = {}", nodes);
        return nodes;
    }

    public XdmNode readNode(final PortReference portReference)
    {
        return Iterables.getOnlyElement(readNodes(portReference));
    }

    public Map<QName, String> readParameters(final PortReference portReference)
    {
        LOG.trace("{@method} portReference = {}", portReference);

        final Builder<QName, String> parameters = ImmutableMap.builder();
        for (final XdmNode parameterNode : readNodes(portReference))
        {
            final XPathCompiler xpathCompiler = getPipelineContext().getProcessor().newXPathCompiler();
            try
            {
                final XPathSelector paramsSelector = xpathCompiler.compile("//.[@name]").load();
                paramsSelector.setContextItem(parameterNode);
                final Iterator<XdmItem> iteratorParams = paramsSelector.iterator();
                while (iteratorParams.hasNext())
                {
                    final XdmNode item = (XdmNode) iteratorParams.next();
                    final Iterable<XdmNode> attributes = SaxonAxis.attributes(item);
                    final Iterator<XdmNode> iterator = attributes.iterator();
                    while (iterator.hasNext())
                    {
                        final XdmNode attribute = iterator.next();
                        final QName attName = attribute.getNodeName();
                        if (!ATTRIBUTE_NAME.equals(attName) && !ATTRIBUTE_NAMESPACE.equals(attName)
                                && !ATTRIBUTE_VALUE.equals(attName))
                        {
                            throw XProcExceptions.xd0014(item);
                        }
                    }
                    final String name = item.getAttributeValue(ATTRIBUTE_NAME);
                    final String namespace = item.getAttributeValue(ATTRIBUTE_NAMESPACE);
                    if (!StringUtils.isEmpty(namespace) && name.contains(":"))
                    {
                        final QName aNode = new QName(StringUtils.substringAfter(name, ":"), item);
                        if (!namespace.equals(aNode.getNamespaceURI()))
                        {
                            throw XProcExceptions.xd0025(SaxonLocation.of(item));
                        }
                    }
                    final String value = item.getAttributeValue(ATTRIBUTE_VALUE);
                    // TODO name should be real QName
                    parameters.put(new QName(namespace, name), value);
                }
            }
            catch (final SaxonApiException e)
            {
                throw new PipelineException(e);
            }
        }

        return parameters.build();
    }

    public XdmNode newParameterElement(final QName name, final String value)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);

        final SaxonBuilder builder = new SaxonBuilder(configuration.getProcessor().getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(ELEMENT_PARAM);
        builder.namespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel.xprocStepNamespace().uri());
        builder.attribute(ATTRIBUTE_NAME, name.toString());
        builder.attribute(ATTRIBUTE_VALUE, value);
        builder.text(value);
        builder.endElement();
        builder.endDocument();

        return builder.getNode();
    }

    public XdmNode newResultElement(final String value)
    {
        Preconditions.checkNotNull(value);

        final SaxonBuilder builder = new SaxonBuilder(configuration.getProcessor().getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(ELEMENT_RESULT);
        builder.namespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel.xprocStepNamespace().uri());
        builder.text(value);
        builder.endElement();
        builder.endDocument();

        return builder.getNode();
    }

    public Iterable<EnvironmentPort> getOutputPorts()
    {
        return Iterables.filter(ports.values(), new Predicate<EnvironmentPort>()
        {
            @Override
            public boolean apply(final EnvironmentPort port)
            {
                return port.getDeclaredPort().isOutput();
            }
        });
    }
}
