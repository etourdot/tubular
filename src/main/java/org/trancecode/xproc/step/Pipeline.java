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
package org.trancecode.xproc.step;

import org.trancecode.xml.Location;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.Port.Type;
import org.trancecode.xproc.parser.StepFactory;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Pipeline extends AbstractCompoundStep
{
	private final QName type;


	public Pipeline(final String name, final Location location, final QName type)
	{
		super(name, location);

		this.type = type;
	}


	protected Pipeline clonePipeline()
	{
		final Pipeline pipeline = new Pipeline(name, location, type);

		for (final Variable variable : variables.values())
		{
			pipeline.declareVariable(variable);
		}

		for (final Port port : ports.values())
		{
			pipeline.addPort(port);
		}

		// TODO parameters?

		for (final Step step : getSteps())
		{
			pipeline.addStep(step);
		}

		return pipeline;
	}


	public StepFactory getFactory()
	{
		return new InvokeStep.Factory(this);
	}


	public QName getType()
	{
		return type;
	}


	public void addImplicitPorts()
	{

		if (getPorts(Type.INPUT).isEmpty())
		{
			declareInputPort(PORT_SOURCE, location, true, true);
		}

		if (getPorts(Type.OUTPUT).isEmpty())
		{
			declareOutputPort(PORT_RESULT, location, true, true);
		}
	}
}
