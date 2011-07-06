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
package org.trancecode.xproc.port;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

import org.trancecode.collection.TcIterables;
import org.trancecode.logging.Logger;
import org.trancecode.xml.AbstractHasLocation;
import org.trancecode.xml.Location;
import org.trancecode.xproc.binding.PipePortBinding;
import org.trancecode.xproc.binding.PortBinding;

/**
 * @author Herve Quiroz
 */
public final class Port extends AbstractHasLocation implements HasPortReference
{
    private static final Logger LOG = Logger.getLogger(Port.class);
    private static final List<PortBinding> EMPTY_PORT_BINDING_LIST = ImmutableList.of();

    private final Type type;
    private final Boolean primary;
    private final Boolean sequence;
    private final List<PortBinding> portBindings;
    private final String select;
    private final PortReference portReference;

    public static enum Type {
        INPUT
        {
            @Override
            public String toString()
            {
                return "input-port";
            }
        },
        OUTPUT
        {
            @Override
            public String toString()
            {
                return "output-port";
            }
        },
        PARAMETER
        {
            @Override
            public String toString()
            {
                return "parameter-port";
            }
        }

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
        return new Port(portReference.setStepName(stepName), location, type, primary, sequence, select, portBindings);
    }

    public String getStepName()
    {
        return portReference.getStepName();
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
        return portReference.getPortName();
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
        LOG.trace("{@method} port = {} ; select = {}", portReference, select);
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    @Override
    public String toString()
    {
        return String.format("%s(%s)", type, portReference);
    }

    public Port setPrimary(final String primary)
    {
        LOG.trace("{@method} port = {} ; primary = {}", portReference, primary);

        if (primary == null)
        {
            return this;
        }

        assert primary.equals(Boolean.TRUE.toString()) || primary.equals(Boolean.FALSE.toString()) : primary;

        return setPrimary(Boolean.parseBoolean(primary));
    }

    public Port setPrimary(final boolean primary)
    {
        LOG.trace("{@method} port = {} ; primary = {} -> {}", portReference, this.primary, primary);
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port setSequence(final String sequence)
    {
        LOG.trace("{@method} port = {} ; sequence = {}", portReference, sequence);

        if (sequence == null)
        {
            return this;
        }

        assert sequence.equals(Boolean.TRUE.toString()) || sequence.equals(Boolean.FALSE.toString()) : sequence;

        return setSequence(Boolean.parseBoolean(sequence));
    }

    public Port setSequence(final boolean sequence)
    {
        LOG.trace("{@method} port = {} ; sequence = {} -> {}", portReference, this.sequence, sequence);
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port setPortBindings(final PortBinding... portBindings)
    {
        return setPortBindings(ImmutableList.copyOf(portBindings));
    }

    public Port setPortBindings(final Iterable<PortBinding> portBindings)
    {
        return new Port(portReference, location, type, primary, sequence, select, portBindings);
    }

    public Port addPortBinding(final PortBinding portBinding)
    {
        return setPortBindings(TcIterables.append(portBindings, portBinding));
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
