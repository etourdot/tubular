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
import org.trancecode.xproc.Port;
import org.trancecode.xproc.PortReference;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcOptions;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.XProcSteps;
import org.trancecode.xproc.XProcUtil;
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

	public static StepFactory FACTORY = new StepFactory()
	{
		public Step newStep(final String name, final Location location)
		{
			return new Store(name, location);
		}
	};


	private Store(final String name, final Location location)
	{
		super(name, location);

		addPort(Port.newInputPort(name, XProcPorts.SOURCE, location));
		addPort(Port.newOutputPort(name, XProcPorts.RESULT, location).setPrimary(false));

		declareVariable(Variable.newOption(XProcOptions.HREF, location).setRequired(true));
		declareVariable(Variable.newOption(XProcOptions.BYTE_ORDER_MARK, location).setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.CDATA_SECTION_ELEMENTS, location).setSelect("''").setRequired(
			false));
		declareVariable(Variable.newOption(XProcOptions.DOCTYPE_PUBLIC, location).setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.DOCTYPE_SYSTEM, location).setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.ENCODING, location).setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.ESCAPE_URI_ATTRIBUTES, location).setSelect("'false'")
			.setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.INCLUDE_CONTENT_TYPE, location).setSelect("'true'")
			.setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.INDENT, location).setSelect("'false'").setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.MEDIA_TYPE, location).setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.METHOD, location).setSelect("'xml'").setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.NORMALIZATION_FORM, location).setSelect("'none'").setRequired(
			false));
		declareVariable(Variable.newOption(XProcOptions.OMIT_XML_DECLARATION, location).setSelect("'true'")
			.setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.STANDALONE, location).setSelect("'omit'").setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.UNDECLARE_PREFIXES, location).setSelect(null)
			.setRequired(false));
		declareVariable(Variable.newOption(XProcOptions.VERSION, location).setSelect("'1.0'").setRequired(false));
	}


	public QName getType()
	{
		return XProcSteps.STORE;
	}


	@Override
	protected Environment doRun(final Environment environment)
	{
		log.entry();

		final XdmNode node = readNode(XProcPorts.SOURCE, environment);
		assert node != null;

		final URI baseUri = environment.getBaseUri();
		final String providedHref = environment.getVariable(XProcOptions.HREF);
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

		final String mimeType = environment.getVariable(XProcOptions.MEDIA_TYPE, DEFAULT_MIMETYPE);

		final String encoding = environment.getVariable(XProcOptions.ENCODING, DEFAULT_ENCODING);

		final String omitXmlDeclaration =
			environment.getVariable(XProcOptions.OMIT_XML_DECLARATION, DEFAULT_OMIT_XML_DECLARATION);

		final String doctypePublicId = environment.getVariable(XProcOptions.DOCTYPE_PUBLIC, DEFAULT_DOCTYPE_PUBLIC);

		final String doctypeSystemId = environment.getVariable(XProcOptions.DOCTYPE_SYSTEM, DEFAULT_DOCTYPE_SYSTEM);

		final String method = environment.getVariable(XProcOptions.METHOD, DEFAULT_METHOD);

		final boolean indent = Boolean.parseBoolean(environment.getVariable(XProcOptions.INDENT));

		log.debug(
			"Storing document to: {} ; mime-type: {} ; encoding: {} ; doctype-public = {} ; doctype-system = {}",
			new Object[] { href, mimeType, encoding, doctypePublicId, doctypeSystemId });

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
			log.debug("method = {}", method);
			serializer.setOutputProperty(Property.METHOD, method);
		}
		serializer.setOutputProperty(Property.ENCODING, encoding);
		serializer.setOutputProperty(Property.MEDIA_TYPE, mimeType);
		serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, omitXmlDeclaration);
		serializer.setOutputProperty(Property.INDENT, (indent ? "yes" : "no"));

		try
		{
			environment.getConfiguration().getProcessor().writeXdmValue(node, serializer);
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

		final XdmNode resultNode =
			XProcUtil.newResultElement(outputUri.toString(), environment.getConfiguration().getProcessor());
		return environment.writeNodes(new PortReference(getName(), XProcPorts.RESULT), resultNode);
	}
}
