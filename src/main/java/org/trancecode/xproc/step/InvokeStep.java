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
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.PortReference;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.binding.PipePortBinding;
import org.trancecode.xproc.parser.StepFactory;

import java.util.Collections;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class InvokeStep extends AbstractCompoundStep
{
	private final Step step;


	public static class Factory implements StepFactory
	{
		private final Pipeline step;


		public Factory(final Pipeline step)
		{
			assert step != null;
			this.step = step;
		}


		@Override
		public Step newStep(final String name, final Location location)
		{
			return new InvokeStep(name, location, step.clonePipeline());
		}
	}


	private InvokeStep(final String name, final Location location, final Step step)
	{
		super(name, location);

		assert step != null;
		this.step = step;

		for (final Port port : step.getPorts().values())
		{
			final Iterable<PortBinding> portBindings;
			if (port.getPortBindings().isEmpty() && (port.isInput() || port.isParameter()))
			{
				final PortReference localPortReference = new PortReference(name, port.getPortName());
				log.trace("{} -> {}", localPortReference, port.getPortReference());
				portBindings = Collections.singleton((PortBinding)new PipePortBinding(localPortReference, location));
			}
			else
			{
				portBindings = port.getPortBindings();
			}

			addPort(Port.newPort(name, port.getPortName(), location, port.getType()).setPortBindings(portBindings));
		}

		addStep(step);
	}


	@Override
	public Step withOption(final QName name, final String select)
	{
		step.withOption(name, select);
		return this;
	}


	@Override
	public Step withOptionValue(final QName name, final String value)
	{
		step.withOptionValue(name, value);
		return this;
	}


	@Override
	public Step withParam(final QName name, final String select, final String value, final Location location)
	{
		step.withParam(name, select, value, location);
		return this;
	}


	@Override
	public QName getType()
	{
		return step.getType();
	}
}
