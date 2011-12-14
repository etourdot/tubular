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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
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
import java.util.Map.Entry;
import java.util.Set;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.api.Nullable;
import org.trancecode.api.ReturnsNullable;
import org.trancecode.collection.TcIterables;
import org.trancecode.collection.TcLists;
import org.trancecode.collection.TcMaps;
import org.trancecode.collection.TcSets;
import org.trancecode.lang.TcObjects;
import org.trancecode.logging.Logger;
import org.trancecode.xml.AbstractHasLocation;
import org.trancecode.xml.Location;
import org.trancecode.xml.saxon.SaxonQNames;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.binding.DataPortBinding;
import org.trancecode.xproc.binding.DocumentPortBinding;
import org.trancecode.xproc.binding.PipePortBinding;
import org.trancecode.xproc.binding.PortBinding;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.PortFunctions;
import org.trancecode.xproc.port.PortPredicates;
import org.trancecode.xproc.port.PortReference;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public final class Step extends AbstractHasLocation implements StepContainer
{
    private static final Logger LOG = Logger.getLogger(Step.class);
    private static final Map<QName, Variable> EMPTY_VARIABLE_LIST = ImmutableMap.of();
    private static final Map<QName, Variable> EMPTY_PARAMETER_MAP = ImmutableMap.of();
    private static final Map<String, Port> EMPTY_PORT_MAP = ImmutableMap.of();
    private static final List<Step> EMPTY_STEP_LIST = ImmutableList.of();
    private static final Iterable<Log> EMPTY_LOG_LIST = ImmutableList.of();

    private final Predicate<Port> PREDICATE_IS_XPATH_CONTEXT_PORT = new Predicate<Port>()
    {
        public boolean apply(final Port port)
        {
            return isXPathContextPort(port);
        }
    };

    private final Map<QName, Variable> parameters;
    private final Map<QName, Variable> variables;

    private final Map<String, Port> ports;

    private final XdmNode node;
    private final QName type;
    private final String name;
    private final String internalName;
    private final StepProcessor stepProcessor;
    private final List<Step> steps;
    private final boolean compoundStep;
    private final Iterable<Log> logs;

    private final Supplier<Integer> hashCode;
    private Map<Step, Iterable<Step>> dependencies;

    public static final class Log
    {
        private final String port;
        private final String href;

        private Log(final String port, @Nullable final String href)
        {
            this.port = Preconditions.checkNotNull(port);
            this.href = href;
        }

        public String getPort()
        {
            return this.port;
        }

        public String getHref()
        {
            return this.href;
        }

        @Override
        public int hashCode()
        {
            return TcObjects.hashCode(port, href);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (o != null && o instanceof Log)
            {
                final Log other = (Log) o;
                return TcObjects.pairEquals(port, other.port, href, other.href);
            }

            return false;
        }

        @Override
        public String toString()
        {
            return String.format("p:log[%s = %s]", port, href);
        }
    }

    public static Step newStep(final QName type, final StepProcessor stepProcessor, final boolean compoundStep)
    {
        return new Step(null, type, null, null, null, stepProcessor, compoundStep, EMPTY_VARIABLE_LIST,
                EMPTY_PARAMETER_MAP, EMPTY_PORT_MAP, EMPTY_STEP_LIST, EMPTY_LOG_LIST);
    }

    public static Step newStep(final XdmNode node, final QName type, final StepProcessor stepProcessor,
            final boolean compoundStep)
    {
        return new Step(node, type, null, null, null, stepProcessor, compoundStep, EMPTY_VARIABLE_LIST,
                EMPTY_PARAMETER_MAP, EMPTY_PORT_MAP, EMPTY_STEP_LIST, EMPTY_LOG_LIST);
    }

    private Step(final XdmNode node, final QName type, final String name, final String internalName,
            final Location location, final StepProcessor stepProcessor, final boolean compoundStep,
            final Map<QName, Variable> variables, final Map<QName, Variable> parameters, final Map<String, Port> ports,
            final Iterable<Step> steps, final Iterable<Log> logs)
    {
        super(location);

        this.node = node;
        this.type = type;
        this.name = name;
        this.internalName = internalName;

        assert stepProcessor != null;
        this.stepProcessor = stepProcessor;

        this.compoundStep = compoundStep;

        this.variables = ImmutableMap.copyOf(variables);
        this.parameters = ImmutableMap.copyOf(parameters);
        this.ports = ImmutableMap.copyOf(ports);
        this.steps = ImmutableList.copyOf(steps);
        this.logs = ImmutableList.copyOf(logs);

        hashCode = TcObjects.immutableObjectHashCode(Step.class, node, type, name, location, stepProcessor,
                compoundStep, variables, parameters, ports, steps);
    }

    public boolean isPipelineStep()
    {
        return isCompoundStep() && !(getStepProcessor() instanceof CoreStepProcessor);
    }

    public Step setName(final String name)
    {
        LOG.trace("{@method} {} -> {}", this.name, name);

        if (TcObjects.equals(this.name, name))
        {
            return this;
        }

        assert internalName == null : internalName;
        final String newInternalName;
        if (isPipelineStep())
        {
            newInternalName = this.name;
            LOG.trace("newInternalName = {}", newInternalName);
        }
        else
        {
            newInternalName = null;
        }
        Step step = new Step(node, type, name, newInternalName, location, stepProcessor, compoundStep, variables,
                parameters, ports, steps, logs);
        for (final Port port : ports.values())
        {
            step = step.withPort(port.setStepName(name));
        }

        return step;
    }

    public boolean isCompoundStep()
    {
        return compoundStep;
    }

    public Step declareVariable(final Variable variable)
    {
        if (variables.containsKey(variable.getName()))
        {
            throw XProcExceptions.xs0004(variable);
        }
        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, TcMaps.copyAndPut(
                variables, variable.getName(), variable), parameters, ports, steps, logs);
    }

    public Step declareVariables(final Map<QName, Variable> variables)
    {
        Step step = this;
        for (final Entry<QName, Variable> variable : variables.entrySet())
        {
            step = step.declareVariable(variable.getValue());
        }

        return step;
    }

    public String getName()
    {
        return name;
    }

    @ReturnsNullable
    public String getInternalName()
    {
        assert internalName == null || isPipelineStep() : internalName;
        return internalName;
    }

    public Step declarePort(final Port port)
    {
        LOG.trace("{@method} step = {} ; port = {}", name, port);
        return declarePorts(ImmutableList.of(port));
    }

    public Step declarePorts(final Iterable<Port> ports)
    {
        LOG.trace("{@method} step = {} ; ports = {}", name, ports);

        final Map<String, Port> newPorts = Maps.newHashMap(this.ports);
        newPorts.putAll(Maps.uniqueIndex(ports, PortFunctions.getPortName()));

        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                newPorts, steps, logs);
    }

    public Port getPort(final String name)
    {
        assert ports.containsKey(name) : "step = " + getName() + " ; port = " + name + " ; ports = " + ports.keySet();
        return ports.get(name);
    }

    public boolean hasPortDeclared(final String name)
    {
        return ports.containsKey(name);
    }

    public Map<String, Port> getPorts()
    {
        return ports;
    }

    private boolean isXPathContextPort(final Port port)
    {
        if (port.isInput() && !port.isParameter())
        {
            if (port.getPortName().equals(XProcPorts.XPATH_CONTEXT))
            {
                return true;
            }

            if (isPrimary(port))
            {
                return !ports.containsKey(XProcPorts.XPATH_CONTEXT);
            }
        }

        return false;
    }

    public Environment run(final Environment environment)
    {
        LOG.trace("{@method} step = {} ; type = {}", name, type);
        return stepProcessor.run(this, environment);
    }

    @ReturnsNullable
    public Port getPrimaryInputPort()
    {
        final List<Port> inputPorts = ImmutableList.copyOf(getInputPorts(false));
        LOG.trace("{@method} inputPorts = {}", inputPorts);
        if (inputPorts.size() == 1)
        {
            final Port inputPort = Iterables.getOnlyElement(inputPorts);
            if (!inputPort.isNotPrimary())
            {
                return inputPort;
            }
        }

        for (final Port inputPort : inputPorts)
        {
            if (inputPort.isPrimary())
            {
                return inputPort;
            }
        }

        return null;
    }

    @ReturnsNullable
    public Port getPrimaryParameterPort()
    {
        final List<Port> parameterPorts = ImmutableList.copyOf(getParameterPorts());
        LOG.trace("parameterPorts = {}", parameterPorts);
        if (parameterPorts.size() == 1)
        {
            final Port parameterPort = Iterables.getOnlyElement(parameterPorts);
            if (!parameterPort.isNotPrimary())
            {
                return parameterPort;
            }
        }

        for (final Port parameterPort : parameterPorts)
        {
            if (parameterPort.isPrimary())
            {
                return parameterPort;
            }
        }

        return null;
    }

    @ReturnsNullable
    public Port getPrimaryOutputPort()
    {
        final List<Port> outputPorts = ImmutableList.copyOf(getOutputPorts());
        LOG.trace("outputPorts = {}", outputPorts);
        if (outputPorts.size() == 1)
        {
            final Port outputPort = Iterables.getOnlyElement(outputPorts);
            if (!outputPort.isNotPrimary())
            {
                return outputPort;
            }
        }

        for (final Port outputPort : outputPorts)
        {
            if (outputPort.isPrimary())
            {
                return outputPort;
            }
        }

        return null;
    }

    private boolean isPrimary(final Port port)
    {
        if (port.isParameter())
        {
            return isPrimary(port, getParameterPorts());
        }

        if (port.isInput())
        {
            return isPrimary(port, getInputPorts());
        }

        assert port.isOutput();
        return isPrimary(port, getOutputPorts());
    }

    private static boolean isPrimary(final Port port, final Iterable<Port> ports)
    {
        assert port != null;

        if (port.isNotPrimary())
        {
            return false;
        }

        if (port.isPrimary())
        {
            return true;
        }

        if (Iterables.size(ports) == 1)
        {
            return true;
        }

        return false;
    }

    public Iterable<Port> getInputPorts()
    {
        return getInputPorts(true);
    }

    public Iterable<Port> getInputPorts(final boolean includeParameterPorts)
    {
        if (includeParameterPorts)
        {
            return Iterables.filter(ports.values(), PortPredicates.isInputPort());
        }
        else
        {
            return Iterables.filter(ports.values(),
                    Predicates.and(PortPredicates.isInputPort(), Predicates.not(PortPredicates.isParameterPort())));
        }
    }

    public Iterable<Port> getOutputPorts()
    {
        return Iterables.filter(ports.values(), PortPredicates.isOutputPort());
    }

    public Iterable<Port> getParameterPorts()
    {
        return Iterables.filter(ports.values(), PortPredicates.isParameterPort());
    }

    public Step withOption(final QName name, final String select, final XdmNode node)
    {
        return withOption(name, select, node, null);
    }

    public Step withOption(final QName name, final String select, final XdmNode node, final PortBinding portBinding)
    {
        final Variable option = variables.get(name);
        Preconditions.checkArgument(option != null, "no such option: %s", name);
        Preconditions.checkArgument(option.isOption(), "not an options: %s", name);

        return new Step(node, type, this.name, internalName, location, stepProcessor, compoundStep, TcMaps.copyAndPut(
                variables, name, option.setSelect(select).setNode(node).setPortBinding(portBinding)), parameters,
                ports, steps, logs);
    }

    public Step withParam(final QName name, final String select, final String value, final Location location)
    {
        return withParam(name, select, value, location, null);
    }

    public Step withParam(final QName name, final String select, final String value, final Location location,
            final XdmNode node)
    {
        return withParam(name, select, value, location, node, null);
    }

    public Step withParam(final QName name, final String select, final String value, final Location location,
            final XdmNode node, final PortBinding portBinding)
    {
        Preconditions.checkArgument(!parameters.containsKey(name), "parameter already set: %s", name);
        return new Step(node, type, this.name, internalName, location, stepProcessor, compoundStep, variables,
                TcMaps.copyAndPut(parameters, name,
                        Variable.newParameter(name, location).setSelect(select).setValue(value).setNode(node)
                                .setPortBinding(portBinding)), ports, steps, logs);
    }

    public Step withOptionValue(final QName name, final String value)
    {
        return withOptionValue(name, value, null);
    }

    public Step withOptionValue(final QName name, final String value, final XdmNode node)
    {
        final Variable option = variables.get(name);
        Preconditions.checkArgument(option != null, "no such option: %s", name);
        Preconditions.checkArgument(option.isOption(), "not an options: %s", name);

        return new Step(node, type, this.name, internalName, location, stepProcessor, compoundStep, TcMaps.copyAndPut(
                variables, name, option.setValue(value).setNode(node)), parameters, ports, steps, logs);
    }

    public boolean hasOptionDeclared(final QName name)
    {
        final Variable variable = variables.get(name);
        return variable != null && variable.isOption();
    }

    @Override
    public String toString()
    {
        if (name != null)
        {
            if (type != null)
            {
                return name + "(" + SaxonQNames.toPrefixString(type) + ")";
            }

            return name;
        }

        return SaxonQNames.toPrefixString(type);
    }

    public Step setPortBindings(final String portName, final PortBinding... portBindings)
    {
        return setPortBindings(portName, ImmutableList.copyOf(portBindings));
    }

    public Step setPortBindings(final String portName, final Iterable<PortBinding> portBindings)
    {
        return withPort(getPort(portName).setPortBindings(portBindings).setSelect(null));
    }

    public Step withPort(final Port port)
    {
        assert ports.containsKey(port.getPortName());

        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                TcMaps.copyAndPut(ports, port.getPortName(), port), steps, logs);
    }

    @ReturnsNullable
    public Port getXPathContextPort()
    {
        final Port xpathContextPort = Iterables.getOnlyElement(
                Iterables.filter(getInputPorts(), PREDICATE_IS_XPATH_CONTEXT_PORT), null);
        LOG.trace("XPath context port = {}", xpathContextPort);
        return xpathContextPort;
    }

    public Map<QName, Variable> getParameters()
    {
        return parameters;
    }

    public Map<QName, Variable> getVariables()
    {
        return variables;
    }

    public QName getType()
    {
        return type;
    }

    public Step setNode(final XdmNode node)
    {
        if (TcObjects.equals(this.node, node))
        {
            return this;
        }
        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                ports, steps, logs);
    }

    public XdmNode getNode()
    {
        return node;
    }

    public Step addChildStep(final Step step)
    {
        LOG.trace("{@method} step = {} ; steps = {} ; childStep = {}", name, steps, step);
        Preconditions.checkNotNull(step);
        Preconditions.checkState(isCompoundStep());

        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                ports, TcIterables.append(steps, step), logs);

    }

    public Step setSubpipeline(final Iterable<Step> steps)
    {
        assert steps != null;
        if (TcObjects.equals(this.steps, steps))
        {
            return this;
        }

        LOG.trace("{@method} step = {} ; steps = {}", name, steps);
        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                ports, steps, logs);
    }

    public List<Step> getSubpipeline()
    {
        return steps;
    }

    public Step setLocation(final Location location)
    {
        if (TcObjects.equals(this.location, location))
        {
            return this;
        }

        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                ports, steps, logs);
    }

    public Variable getVariable(final QName name)
    {
        return variables.get(name);
    }

    public PortReference getPortReference(final String portName)
    {
        return PortReference.newReference(name, portName);
    }

    public StepProcessor getStepProcessor()
    {
        return stepProcessor;
    }

    @ReturnsNullable
    private ExternalResources getExternalResources()
    {
        return stepProcessor.getClass().getAnnotation(ExternalResources.class);
    }

    protected boolean readsExternalResources()
    {
        final ExternalResources externalResources = getExternalResources();
        if (externalResources != null)
        {
            if (externalResources.read())
            {
                return true;
            }
        }
        else
        {
            return true;
        }

        for (final Port inputPort : getInputPorts())
        {
            for (final PortBinding portBinding : inputPort.getPortBindings())
            {
                if (portBinding instanceof DocumentPortBinding || portBinding instanceof DataPortBinding)
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean writesExternalResources()
    {
        final ExternalResources externalResources = getExternalResources();
        if (externalResources != null)
        {
            return externalResources.write();
        }

        return true;
    }

    protected Map<Step, Iterable<Step>> getSubpipelineStepDependencies()
    {
        Preconditions.checkState(isCompoundStep(), "not a compound step: %s", getName());
        if (dependencies == null)
        {
            dependencies = getSubpipelineStepDependencies(getSubpipeline());
        }

        return dependencies;
    }

    protected static Map<Step, Iterable<Step>> getSubpipelineStepDependencies(final Iterable<Step> steps)
    {
        LOG.trace("{@method} steps = {}", steps);

        Step lastWriteStep = null;
        Step defaultReadblePortStep = null;
        final Map<String, Step> subpipelineStepByName = Maps.newHashMap();
        for (final Step step : steps)
        {
            subpipelineStepByName.put(step.getName(), step);
        }

        final Map<Step, Iterable<Step>> dependencies = Maps.newHashMap();
        for (final Step step : steps)
        {
            LOG.trace("  step {}", step);

            final List<Step> stepDependencies = Lists.newArrayListWithExpectedSize(16);
            dependencies.put(step, stepDependencies);

            // dependencies related to external resources
            if (lastWriteStep != null && step.readsExternalResources())
            {
                LOG.trace("  step {} reads external resources and thus from output of step {}", step, lastWriteStep);
                stepDependencies.add(lastWriteStep);
            }

            // dependencies related to port binding (pipe)
            for (final Port inputPort : step.getInputPorts())
            {
                if (defaultReadblePortStep != null && inputPort.getPortBindings().isEmpty()
                        && (step.isPrimary(inputPort) || step.isXPathContextPort(inputPort)))
                {
                    LOG.trace("  step {} reads implicitly from output of step {}", step, defaultReadblePortStep);
                    stepDependencies.add(defaultReadblePortStep);
                }
                else
                {
                    for (final PipePortBinding portBinding : Iterables.filter(inputPort.getPortBindings(),
                            PipePortBinding.class))
                    {
                        final PortReference dependencyPortReference = portBinding.getPortReference();
                        LOG.trace("  step {} reads from output port {}", step, dependencyPortReference);
                        final Step dependency = subpipelineStepByName.get(dependencyPortReference.getStepName());
                        if (dependency != null)
                        {
                            stepDependencies.add(dependency);
                        }
                    }
                }
            }

            // dependencies related to variable binding (pipe)
            for (final Variable variable : step.getVariables().values())
            {
                if (variable.getPortBinding() != null && variable.getPortBinding() instanceof PipePortBinding)
                {
                    final PipePortBinding portBinding = (PipePortBinding) variable.getPortBinding();
                    final PortReference dependencyPortReference = portBinding.getPortReference();
                    LOG.trace("  variable {} in step {} reads from output port {}", variable.getName(), step,
                            dependencyPortReference);
                    final Step dependency = subpipelineStepByName.get(dependencyPortReference.getStepName());
                    if (dependency != null)
                    {
                        stepDependencies.add(dependency);
                    }
                }
            }

            LOG.trace("  => stepDependencies = {}", stepDependencies);

            // update current information about the step
            if (step.writesExternalResources())
            {
                lastWriteStep = step;
            }
            if (step.getPrimaryOutputPort() != null)
            {
                defaultReadblePortStep = step;
            }
        }

        return dependencies;
    }

    @Override
    public int hashCode()
    {
        return hashCode.get();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (o != null && o instanceof Step)
        {
            final Step other = (Step) o;
            return TcObjects.pairEquals(node, other.node, type, other.type, name, other.name, location, other.location,
                    stepProcessor, other.stepProcessor, compoundStep, other.compoundStep, variables, other.variables,
                    parameters, other.parameters, ports, other.ports, steps, other.steps, logs, other.logs);
        }

        return false;
    }

    @Override
    public Iterable<Step> getAllSteps()
    {
        return Iterables.concat(ImmutableList.of(this),
                Iterables.concat(Iterables.transform(getSubpipeline(), new Function<Step, Iterable<Step>>()
                {
                    @Override
                    public Iterable<Step> apply(final Step step)
                    {
                        return step.getAllSteps();
                    }
                })));
    }

    @Override
    public Step getStepByName(final String name)
    {
        return Iterables.find(getAllSteps(), StepPredicates.hasName(name));
    }

    public Iterable<Log> getLogs()
    {
        return logs;
    }

    public Step addLog(final String port, final String href)
    {
        LOG.trace("{@method} step = {} ; port = {} ; href = {}", name, port, href);
        final Log log = new Log(port, href);
        assert !Iterables.contains(logs, log) : name + " / " + logs + " / " + log;
        return new Step(node, type, name, internalName, location, stepProcessor, compoundStep, variables, parameters,
                ports, steps, TcLists.immutableList(logs, log));
    }

    public boolean hasLogDeclaredForPort(final String port)
    {
        return Iterables.any(logs, new Predicate<Log>()
        {
            @Override
            public boolean apply(final Log log)
            {
                return log.port.equals(port);
            }
        });
    }

    private void checkCycleDependencies(final Step childStep, final Collection<Step> dependingSteps)
    {
        LOG.trace("{@method} childStep = {} ; dependingSteps = {}", childStep, dependingSteps);
        final Collection<Step> newDependingSteps = TcSets.immutableSet(dependingSteps, childStep);
        for (final Step dependencyStep : getSubpipelineStepDependencies().get(childStep))
        {
            if (newDependingSteps.contains(dependencyStep))
            {
                throw XProcExceptions.xs0001(childStep);
            }

            checkCycleDependencies(dependencyStep, newDependingSteps);
        }
    }

    /**
     * {@code err:XS0001}.
     */
    private void checkCyclicDependencies()
    {
        LOG.trace("{@method} step = {} ; subpipeline = {}", this, getSubpipeline());
        final Collection<Step> empty = ImmutableSet.of();
        for (final Step childStep : getSubpipeline())
        {
            checkCycleDependencies(childStep, empty);
        }
    }

    /**
     * {@code err:XS0002}.
     */
    private void checkStepNames()
    {
        final Set<String> stepNames = Sets.newHashSet();
        for (final Step childStep : getSubpipeline())
        {
            if (stepNames.contains(childStep.getName()))
            {
                throw XProcExceptions.xs0002(childStep);
            }

            stepNames.add(childStep.getName());
        }
    }

    /**
     * {@code err:XS0003}.
     */
    private void checkInputPorts()
    {
        for (final Port inputPort : getInputPorts())
        {
            if (!isPrimary(inputPort) && inputPort.getPortBindings().isEmpty())
            {
                throw XProcExceptions.xs0003(this, inputPort.getPortName());
            }
        }
    }

    private Iterable<PortBinding> getInputPortBindings()
    {
        return Iterables.concat(Iterables.transform(getInputPorts(true), PortFunctions.getPortBindings()));
    }

    /**
     * {@code err:XS0005}.
     */
    private void checkOutputPorts()
    {
        for (final Port outputPort : getOutputPorts())
        {
            if (outputPort.getPortBindings().isEmpty())
            {
                final boolean connected = Iterables.any(getDescendantSteps(), new Predicate<Step>()
                {
                    @Override
                    public boolean apply(final Step step)
                    {
                        return Iterables.any(step.getInputPortBindings(), new Predicate<PortBinding>()
                        {
                            @Override
                            public boolean apply(final PortBinding portBinding)
                            {
                                if (portBinding instanceof PipePortBinding)
                                {
                                    final PipePortBinding pipe = (PipePortBinding) portBinding;
                                    return pipe.getPortReference().equals(outputPort.getPortReference());
                                }

                                return false;
                            }
                        });
                    }
                });
                if (!connected)
                {
                    throw XProcExceptions.xs0005(this, outputPort.getPortName());
                }
            }
        }
    }

    private Iterable<Step> getDescendantSteps()
    {
        return Iterables.concat(Iterables.transform(getSubpipeline(), new Function<Step, Iterable<Step>>()
        {
            @Override
            public Iterable<Step> apply(final Step step)
            {
                return Iterables.concat(ImmutableList.of(step), step.getDescendantSteps());
            }
        }));
    }

    public void checkDeclaredStep()
    {
        checkCyclicDependencies();
        checkStepNames();
    }

    public void checkInstanceStep()
    {
        checkInputPorts();
        checkOutputPorts();
    }
}
