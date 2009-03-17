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

import org.trancecode.xml.Location;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Port extends AbstractHasLocation
{
	private final Type type;
	private final boolean primary;
	private final boolean sequence;
	protected final List<PortBinding> portBindings = new ArrayList<PortBinding>();
	private String select;
	private final PortReference portReference;


	public static enum Type
	{
		INPUT, OUTPUT, PARAMETER
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
		return new Port(stepName, portName, location, type, false, false);
	}


	private Port(
		final String stepName, final String portName, final Location location, final Type type, final boolean primary,
		final boolean sequence)
	{
		this(new PortReference(stepName, portName), location, type, primary, sequence);
	}


	public Port(
		final PortReference portReference, final Location location, final Type type, final boolean primary,
		final boolean sequence)
	{
		super(location);

		this.type = type;
		this.primary = primary;
		this.sequence = sequence;
		this.portReference = portReference;
	}


	public String getStepName()
	{
		return portReference.stepName;
	}


	public List<PortBinding> getPortBindings()
	{
		return portBindings;
	}


	public boolean isInput()
	{
		return type == Type.INPUT;
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


	public PortReference getPortReference()
	{
		return portReference;
	}


	public String getPortName()
	{
		return portReference.portName;
	}


	public boolean isPrimary()
	{
		return primary;
	}


	public boolean isSequence()
	{
		return sequence;
	}


	public String getSelect()
	{
		return select;
	}


	public void setSelect(final String select)
	{
		this.select = select;
	}


	@Override
	public String toString()
	{
		return String.format(
			"%s[%s][%s/%s]", getClass().getSimpleName(), type, portReference.stepName, portReference.portName);
	}


	public Port setPrimary(final boolean primary)
	{
		return new Port(portReference, location, type, primary, sequence);
	}


	public Port setSequence(final boolean sequence)
	{
		return new Port(portReference, location, type, primary, sequence);
	}
}
