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
import org.trancecode.xml.XmlUtil;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
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
	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new Load(name, location);
		}
	};


	private Load(final String name, final Location location)
	{
		super(name, location);

		addPort(Port.newOutputPort(name, XProcPorts.RESULT, location));

		declareVariable(Variable.newOption(XProcOptions.HREF, location).setRequired(true));
		declareVariable(Variable.newOption(XProcOptions.DTD_VALIDATE, location).setSelect("'false'").setRequired(false));
	}


	public QName getType()
	{
		return XProcSteps.LOAD;
	}


	@Override
	protected Environment doRun(final Environment environment)
	{
		log.entry();

		final String href = environment.getVariable(XProcOptions.HREF);
		assert href != null;
		log.trace("href = {}", href);

		final boolean validate = Boolean.parseBoolean(environment.getVariable(XProcOptions.VALIDATE));
		log.trace("validate = {}", validate);

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
			document = environment.getConfiguration().getProcessor().newDocumentBuilder().build(source);
		}
		catch (final SaxonApiException e)
		{
			throw new PipelineException("Error while trying to build document ; href = %s", e, href);
		}
		finally
		{
			XmlUtil.closeQuietly(source, log);
		}

		return environment.writeNodes(getName(), XProcPorts.RESULT, document);
	}
}
