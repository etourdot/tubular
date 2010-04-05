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

import org.trancecode.logging.Logger;
import org.trancecode.xml.AbstractHasLocation;
import org.trancecode.xml.Location;
import org.trancecode.xproc.binding.PipePortBinding;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Port extends AbstractHasLocation implements HasPortReference
{
    private static final Logger LOG = Logger.getLogger(Port.class);
    private static final List<PortBinding> EMPTY_PORT_BINDING_LIST = ImmutableList.of();

    private final Type type;
    private final Boolean primary;
    private final Boolean sequence;
    protected final List<PortBinding> portBindings;
    private final String select;
    private final PortReference portReference;

    public static enum Type {
        INPUT, OUTPUT, PARAMETER
    }

    public static Port newInputPort(final String portName)
    {
        return newInputPort(null, portName, null);
    }

    public static Port newParameterPort(final String portName)
    {
        return newParameterPort(null, portName, null);
    }

    public static Port newOutputPort(final String portName)
    {
        return newOutputPort(null, portName, null);
    }

    public static Port newInputPort(final String stepName, final String portName, final Location location)
    {
        return newPort(stepName, portName, location, Type.INPUT);
    }

    public static Port newParameterPort(final String stepName, final String portName, final Location location)
    {
        return newPort(stepName, portName, location, Type.PARAMETER);
    }

    public static Port newOutputPort(final String stepName, final String portName, final Location location)
    {
        return newPort(stepName, portName, location, Type.OUTPUT);
    }

    public static Port newPort(final String stepName, final String portName, final Location location, final Type type)
    {
        return new Port(stepName, portName, location, type);
    }

    private Port(final String stepName, final String portName, final Location location, final Type type)
    {
        this(PortReference.newReference(stepName, portName), location, type, null, null, null, EMPTY_PORT_BINDING_LIST);
    }

    private Port(final PortReference portReference, final Location location, final Type type, final Boolean primary,
            final Boolean sequence, final String select, final Iterable<PortBinding> portBindings)
    {
        super(location);

        Preconditions.checkNotNull(portReference);
        this.portReference = portReference;
        this.type = type;
        this.primary = primary;
        this.sequence = sequence;
        this.select = select;
        Preconditions.checkNotNull(portBindings);
        this.portBindings = ImmutableList.copyOf(portBindings);
    }

    public Port setLocation(final Location location)
    {
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port setStepName(final String stepName)
    {
        return new Port(portReference.setStep(stepName), location, type, primary, sequence, select, portBindings);
    }

    public String getStepName()
    {
        return portReference.step();
    }

    public List<PortBinding> getPortBindings()
    {
        return portBindings;
    }

    public boolean isInput()
    {
        return type == Type.INPUT || type == Type.PARAMETER;
    }

    public boolean isOutput()
    {
        return type == Type.OUTPUT;
    }

    public boolean isParameter()
    {
        return type == Type.PARAMETER;
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public PortReference getPortReference()
    {
        return portReference;
    }

    public String getPortName()
    {
        return portReference.port();
    }

    public boolean isPrimary()
    {
        return primary != null && primary;
    }

    public boolean isNotPrimary()
    {
        return primary != null && !primary;
    }

    public boolean isSequence()
    {
        return sequence != null && sequence;
    }

    public String getSelect()
    {
        return select;
    }

    public Port setSelect(final String select)
    {
        LOG.trace("port = {} ; select = {}", portReference, select);
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    private String getTag(final Boolean value, final String whenTrue, final String whenFalse)
    {
        if (value == null)
        {
            return "";
        }

        if (value)
        {
            return whenTrue;
        }

        return whenFalse;
    }

    @Override
    public String toString()
    {
        final String primaryTag = getTag(primary, "[primary]", "[not primary]");
        final String sequenceTag = getTag(sequence, "[sequence]", "[not sequence]");
        final String parameterTag = getTag(isParameter(), "[parameter]", "");
        final String selectTag = (select != null ? "[select = " + select + "]" : "");
        return String.format("%s[%s][%s]%s%s%s%s", getClass().getSimpleName(), type, portReference, parameterTag,
                primaryTag, sequenceTag, selectTag);
    }

    public Port setPrimary(final String primary)
    {
        LOG.trace("port = {} ; primary = {}", portReference, primary);

        if (primary == null)
        {
            return this;
        }

        assert primary.equals(Boolean.TRUE.toString()) || primary.equals(Boolean.FALSE.toString()) : primary;

        return setPrimary(Boolean.parseBoolean(primary));
    }

    public Port setPrimary(final boolean primary)
    {
        LOG.trace("port = {}", portReference);
        LOG.trace("{} -> {}", this.primary, primary);

        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port setSequence(final String sequence)
    {
        LOG.trace("port = {}", portReference);
        LOG.trace("sequence = {}", sequence);

        if (sequence == null)
        {
            return this;
        }

        assert sequence.equals(Boolean.TRUE.toString()) || sequence.equals(Boolean.FALSE.toString()) : sequence;

        return setSequence(Boolean.parseBoolean(sequence));
    }

    public Port setSequence(final boolean sequence)
    {
        LOG.trace("port = {}", portReference);
        LOG.trace("{} -> {}", this.sequence, sequence);

        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port setPortBindings(final PortBinding... portBindings)
    {
        return setPortBindings(ImmutableList.of(portBindings));
    }

    public Port setPortBindings(final Iterable<PortBinding> portBindings)
    {
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port pipe(final String stepName, final String portName, final Location location)
    {
        return setPortBindings(new PipePortBinding(stepName, portName, location));
    }

    public Port pipe(final Port port)
    {
        return pipe(port.getStepName(), port.getPortName(), port.getLocation());
    }
}
