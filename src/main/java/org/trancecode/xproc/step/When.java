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
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class When extends AbstractCompoundStep
{
	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new When(name, location);
		}
	}


	public When(final String name, final Location location)
	{
		super(name, location);

		declareInputPort(PORT_SOURCE, location, true, true);
		declareInputPort(PORT_XPATH_CONTEXT, location, false, false);

		declareOutputPort(PORT_RESULT, location, true, true);

		declareOption(OPTION_TEST, null, false, location);
	}


	public boolean test(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);
		final boolean result = doTest(environment);
		log.trace("result = %s", result);
		return result;
	}


	private boolean doTest(final Environment environment)
	{
		final Environment resultEnvironment = environment.newChildStepEnvironment();
		getEnvironmentPort(PORT_XPATH_CONTEXT, resultEnvironment);
		final String test = variables.get(OPTION_TEST).getValue();
		log.trace("test = %s", test);
		final XdmValue result = evaluateXPath(test, resultEnvironment);
		log.trace("result = %s", result);

		if (result.size() == 0)
		{
			return false;
		}

		if (result.size() > 1)
		{
			return true;
		}

		final XdmItem resultNode = result.iterator().next();
		if (resultNode.isAtomicValue())
		{
			try
			{
				return ((XdmAtomicValue)resultNode).getBooleanValue();
			}
			catch (final SaxonApiException e)
			{
				// TODO error handling
				throw new IllegalStateException(e);
			}
		}

		return true;
	}


	@Override
	public QName getType()
	{
		return XProcSteps.STEP_WHEN;
	}
}
