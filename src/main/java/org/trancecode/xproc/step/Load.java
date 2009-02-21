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
package org.trancecode.xproc.step;

import org.trancecode.xml.Location;
import org.trancecode.xml.XmlUtil;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.parser.StepFactory;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Load extends AbstractStep
{
	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new Load(name, location);
		}


		public QName getStepType()
		{
			return STEP_LOAD;
		}
	}


	private Load(final String name, final Location location)
	{
		super(name, location);

		declareOutputPort(PORT_RESULT, location, false, false);
		declareOption(OPTION_HREF, null, true, location);
		declareOption(OPTION_DTD_VALIDATE, "'false'", false, location);
	}


	public QName getType()
	{
		return STEP_LOAD;
	}


	@Override
	protected void doRun(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		final String href = environment.getVariable(OPTION_HREF);
		assert href != null;
		log.trace("href = %s", href);

		final boolean validate = Boolean.parseBoolean(environment.getVariable(OPTION_VALIDATE));
		log.trace("validate = %s", validate);

		final Source source;
		try
		{
			source = environment.getConfiguration().getUriResolver().resolve(href, environment.getBaseUri().toString());
		}
		catch (final Exception e)
		{
			throw new PipelineException("Error while trying to read document ; href = %s ; baseUri = %s", e, href,
				environment.getBaseUri());
		}

		final XdmNode document;
		try
		{
			document = environment.getProcessor().newDocumentBuilder().build(source);
		}
		catch (final SaxonApiException e)
		{
			throw new PipelineException("Error while trying to build document ; href = %s", e, href);
		}
		finally
		{
			XmlUtil.closeQuietly(source, log);
		}

		writeNodes(PORT_RESULT, environment, document);
	}
}
