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
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.parser.StepFactory;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Otherwise extends When
{
	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new Otherwise(name, location);
		}
	};


	public Otherwise(final String name, final Location location)
	{
		super(name, location);

		withOption(OPTION_TEST, "'true'");
	}


	@Override
	public boolean test(final Environment environment)
	{
		log.entry();

		return true;
	}


	@Override
	public QName getType()
	{
		return XProcSteps.OTHERWISE;
	}
}
