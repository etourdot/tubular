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
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;

import com.google.common.collect.ImmutableList;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class StoreStepFactory extends AbstractStepFactory
{
	private static final Iterable<Port> PORTS =
		ImmutableList.of(Port.newInputPort(XProcPorts.SOURCE), Port.newOutputPort(XProcPorts.RESULT).setPrimary(false));

	private static final Iterable<Variable> VARIABLES =
		ImmutableList.of(
			Variable.newOption(XProcOptions.HREF).setRequired(true), Variable.newOption(XProcOptions.BYTE_ORDER_MARK)
				.setRequired(false), Variable.newOption(XProcOptions.CDATA_SECTION_ELEMENTS).setSelect("''")
				.setRequired(false), Variable.newOption(XProcOptions.DOCTYPE_PUBLIC).setRequired(false), Variable
				.newOption(XProcOptions.DOCTYPE_SYSTEM).setRequired(false), Variable.newOption(XProcOptions.ENCODING)
				.setRequired(false), Variable.newOption(XProcOptions.ESCAPE_URI_ATTRIBUTES).setSelect("'false'")
				.setRequired(false), Variable.newOption(XProcOptions.INCLUDE_CONTENT_TYPE).setSelect("'true'")
				.setRequired(false), Variable.newOption(XProcOptions.INDENT).setSelect("'false'").setRequired(false),
			Variable.newOption(XProcOptions.MEDIA_TYPE).setRequired(false), Variable.newOption(XProcOptions.METHOD)
				.setSelect("'xml'").setRequired(false), Variable.newOption(XProcOptions.NORMALIZATION_FORM).setSelect(
				"'none'").setRequired(false), Variable.newOption(XProcOptions.OMIT_XML_DECLARATION).setSelect("'true'")
				.setRequired(false),
			Variable.newOption(XProcOptions.STANDALONE).setSelect("'omit'").setRequired(false), Variable.newOption(
				XProcOptions.UNDECLARE_PREFIXES).setSelect(null).setRequired(false), Variable.newOption(
				XProcOptions.VERSION).setSelect("'1.0'").setRequired(false));

	public static StoreStepFactory INSTANCE = new StoreStepFactory();


	public StoreStepFactory()
	{
		super(XProcSteps.STORE, StoreStepProcessor.INSTANCE, false, PORTS, VARIABLES);
	}
}
