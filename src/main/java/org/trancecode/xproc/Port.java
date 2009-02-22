/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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
	private final String portName;
	private final String stepName;
	private String select;
	private final PortReference portReference;


	public static enum Type
	{
		INPUT, OUTPUT, PARAMETER
	}


	public Port(
		final String stepName, final String portName, final Location location, final Type type, final boolean primary,
		final boolean sequence)
	{
		super(location);

		this.stepName = stepName;
		this.type = type;
		this.primary = primary;
		this.sequence = sequence;
		this.portName = portName;
		portReference = new PortReference(stepName, portName);
	}


	public String getStepName()
	{
		return stepName;
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
		return portName;
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
		return String.format("%s[%s][%s/%s]", getClass().getSimpleName(), type, stepName, portName);
	}
}
