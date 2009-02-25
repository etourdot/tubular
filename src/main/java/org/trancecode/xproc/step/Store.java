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

import org.trancecode.io.IOUtil;
import org.trancecode.io.MediaTypes;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.parser.StepFactory;

import java.io.OutputStream;
import java.net.URI;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Serializer.Property;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Store extends AbstractStep
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String DEFAULT_OMIT_XML_DECLARATION = "no";

	public static final String DEFAULT_DOCTYPE_PUBLIC = null;

	public static final String DEFAULT_DOCTYPE_SYSTEM = null;

	public static final String DEFAULT_METHOD = null;

	public static final String DEFAULT_MIMETYPE = MediaTypes.MEDIA_TYPE_XML;


	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new Store(name, location);
		}
	}


	private Store(final String name, final Location location)
	{
		super(name, location);

		declareInputPort(PORT_SOURCE, location, false, false);
		declareOutputPort(PORT_RESULT, location, false, false);
		declareOption(OPTION_HREF, null, true, location);
		declareOption(OPTION_BYTE_ORDER_MARK, null, false, location);
		declareOption(OPTION_CDATA_SECTION_ELEMENTS, "''", false, location);
		declareOption(OPTION_DOCTYPE_PUBLIC, null, false, location);
		declareOption(OPTION_DOCTYPE_SYSTEM, null, false, location);
		declareOption(OPTION_ENCODING, null, false, location);
		declareOption(OPTION_ESCAPE_URI_ATTRIBUTES, "'false'", false, location);
		declareOption(OPTION_INCLUDE_CONTENT_TYPE, "'true'", false, location);
		declareOption(OPTION_INDENT, "'false'", false, location);
		declareOption(OPTION_MEDIA_TYPE, null, false, location);
		declareOption(OPTION_METHOD, "'xml'", false, location);
		declareOption(OPTION_NORMALIZATION_FORM, "'none'", false, location);
		declareOption(OPTION_OMIT_XML_DECLARATION, "'true'", false, location);
		declareOption(OPTION_STANDALONE, "'omit'", false, location);
		declareOption(OPTION_UNDECLARE_PREFIXES, null, false, location);
		declareOption(OPTION_VERSION, "'1.0'", false, location);
	}


	public QName getType()
	{
		return STEP_STORE;
	}


	@Override
	protected void doRun(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		final XdmNode node = readNode(PORT_SOURCE, environment);
		assert node != null;

		final URI baseUri = environment.getBaseUri();
		final String providedHref = environment.getVariable(OPTION_HREF);
		final String href;
		if (providedHref != null)
		{
			href = providedHref;
		}
		else
		{
			href = node.getUnderlyingNode().getSystemId();
		}

		final URI outputUri = baseUri.resolve(href);

		final String mimeType = getVariable(OPTION_MEDIA_TYPE, environment, DEFAULT_MIMETYPE);

		final String encoding = getVariable(OPTION_ENCODING, environment, DEFAULT_ENCODING);

		final String omitXmlDeclaration =
			getVariable(OPTION_OMIT_XML_DECLARATION, environment, DEFAULT_OMIT_XML_DECLARATION);

		final String doctypePublicId = getVariable(OPTION_DOCTYPE_PUBLIC, environment, DEFAULT_DOCTYPE_PUBLIC);

		final String doctypeSystemId = getVariable(OPTION_DOCTYPE_SYSTEM, environment, DEFAULT_DOCTYPE_SYSTEM);

		final String method = getVariable(OPTION_METHOD, environment, DEFAULT_METHOD);

		final boolean indent = Boolean.parseBoolean(environment.getVariable(OPTION_INDENT));

		log.debug(
			"Storing document to: %s ; mime-type: %s ; encoding: %s ; doctype-public = %s ; doctype-system = %s", href,
			mimeType, encoding, doctypePublicId, doctypeSystemId);

		assert environment.getConfiguration().getOutputResolver() != null;
		final OutputStream targetOutputStream =
			environment.getConfiguration().getOutputResolver().resolveOutputStream(
				href, environment.getBaseUri().toString());

		final Serializer serializer = new Serializer();
		serializer.setOutputStream(targetOutputStream);
		if (doctypePublicId != null)
		{
			serializer.setOutputProperty(Property.DOCTYPE_PUBLIC, doctypePublicId);
		}
		if (doctypeSystemId != null)
		{
			serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
		}
		serializer.setOutputProperty(Property.DOCTYPE_SYSTEM, doctypeSystemId);
		if (method != null)
		{
			log.debug("method = %s", method);
			serializer.setOutputProperty(Property.METHOD, method);
		}
		serializer.setOutputProperty(Property.ENCODING, encoding);
		serializer.setOutputProperty(Property.MEDIA_TYPE, mimeType);
		serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, omitXmlDeclaration);
		serializer.setOutputProperty(Property.INDENT, (indent ? "yes" : "no"));

		try
		{
			environment.getProcessor().writeXdmValue(node, serializer);
		}
		catch (final Exception e)
		{
			throw new PipelineException("Error while trying to write document ; output-base-uri = %s", e, outputUri);
		}
		finally
		{
			// FIXME should not be quiet here
			IOUtil.closeQuietly(targetOutputStream);
		}

		final XdmNode resultNode = newResultElement(outputUri.toString(), environment.getProcessor());
		writeNodes(PORT_RESULT, environment, resultNode);
	}
}
