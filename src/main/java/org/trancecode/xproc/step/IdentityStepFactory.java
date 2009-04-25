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

import org.trancecode.xproc.Port;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;

import com.google.common.collect.ImmutableSet;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class IdentityStepFactory extends AbstractStepFactory
{
	private static final Iterable<Port> PORTS =
		ImmutableSet.of(Port.newInputPort(DEFAULT_STEP_NAME, XProcPorts.SOURCE, null).setSequence(true), Port
			.newOutputPort(DEFAULT_STEP_NAME, XProcPorts.RESULT, null).setSequence(true));

	public static final IdentityStepFactory INSTANCE = new IdentityStepFactory();


	public IdentityStepFactory()
	{
		super(XProcSteps.IDENTITY, IdentityStepProcessor.INSTANCE, false, PORTS, EMPTY_VARIABLE_LIST);
	}
}
