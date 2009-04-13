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

import org.trancecode.io.UriUtil;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.parser.StepFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class XslFormatter extends AbstractStep
{
	public static final String DEFAULT_CONTENT_TYPE = "application/pdf";

	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new XslFormatter(name, null, location);
		}
	};


	private XslFormatter(final String name, final String libraryName, final Location location)
	{
		super(name, location);

		addPort(Port.newInputPort(name, XProcPorts.SOURCE, location));
		addPort(Port.newParameterPort(name, XProcPorts.PARAMETERS, location));
		addPort(Port.newOutputPort(name, XProcPorts.RESULT, location).setPrimary(false));

		declareVariable(Variable.newOption(XProcOptions.HREF, location).setRequired(true));
		declareVariable(Variable.newOption(XProcOptions.CONTENT_TYPE, location).setRequired(false));
	}


	public QName getType()
	{
		return XProcSteps.XSL_FORMATTER;
	}


	@Override
	protected Environment doRun(final Environment environment) throws Exception
	{
		log.entry();

		final XdmNode source = readNode(XProcPorts.SOURCE, environment);

		final String href = environment.getVariable(XProcOptions.CONTENT_TYPE, null);
		assert href != null;
		final OutputStream resultOutputStream =
			environment.getConfiguration().getOutputResolver()
				.resolveOutputStream(href, source.getBaseURI().toString());

		final String contentType = environment.getVariable(XProcOptions.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
		final FopFactory fopFactory = FopFactory.newInstance();
		final Fop fop = fopFactory.newFop(contentType, resultOutputStream);
		fop.getUserAgent().setURIResolver(new URIResolver()
		{
			@Override
			public Source resolve(final String href, final String base) throws TransformerException
			{
				final URI uri = UriUtil.resolve(href, base);
				final InputStream inputStream =
					environment.getConfiguration().getInputResolver().resolveInputStream(href, base);
				return new StreamSource(inputStream, uri.toString());
			}
		});
		fop.getUserAgent().setBaseURL(source.getBaseURI().toString());

		final SAXResult fopResult = new SAXResult(fop.getDefaultHandler());

		// TODO run FOP
		// TODO build result
		return null;
	}
}
