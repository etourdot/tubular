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

import org.trancecode.core.CollectionUtil;
import org.trancecode.io.UriUtil;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcException.Type;
import org.trancecode.xproc.parser.StepFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;

import net.sf.saxon.OutputURIResolver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Xslt extends AbstractStep
{
	public static final String DEFAULT_VERSION = "2.0";
	public static final List<String> SUPPORTED_VERSIONS = Arrays.asList("2.0");


	public static class Factory implements StepFactory
	{
		public Step newStep(final String name, final Location location)
		{
			return new Xslt(name, location);
		}
	}


	private Xslt(final String name, final Location location)
	{
		super(name, location);

		declareInputPort(PORT_SOURCE, location, true, true);
		declareInputPort(PORT_STYLESHEET, location, false, false);
		declareParameterPort(PORT_PARAMETERS, location, false, false);
		declareOutputPort(PORT_RESULT, location, true, false);
		declareOutputPort(PORT_SECONDARY, location, false, true);

		declareOption(OPTION_INITIAL_MODE, null, false, location);
		declareOption(OPTION_TEMPLATE_NAME, null, false, location);
		declareOption(OPTION_OUTPUT_BASE_URI, null, false, location);
		declareOption(OPTION_VERSION, null, false, location);
	}


	public QName getType()
	{
		return STEP_XSLT;
	}


	@Override
	protected void doRun(final Environment environment)
	{
		log.trace("%s", METHOD_NAME);

		final XdmNode sourceDocument = readNode(PORT_SOURCE, environment);
		assert sourceDocument != null;

		final String providedOutputBaseUri = environment.getVariable(OPTION_OUTPUT_BASE_URI);
		final URI outputBaseUri;
		if (providedOutputBaseUri != null && providedOutputBaseUri.length() > 0)
		{
			outputBaseUri = URI.create(providedOutputBaseUri);
		}
		else if (sourceDocument.getBaseURI() != null)
		{
			outputBaseUri = sourceDocument.getBaseURI();
		}
		else
		{
			outputBaseUri = environment.getBaseUri();
		}
		assert outputBaseUri != null;

		final String version = getVariable(OPTION_VERSION, environment, DEFAULT_VERSION);

		if (!SUPPORTED_VERSIONS.contains(version))
		{
			throw new XProcException(Type.STEP, 38, getLocation(), "XSLT version %s not supported", version);
		}
		final XdmNode stylesheet = readNode(PORT_STYLESHEET, environment);
		assert stylesheet != null;

		final Processor processor = environment.getProcessor();

		// TODO pipeline logging
		final XsltTransformer transformer;
		try
		{
			transformer = processor.newXsltCompiler().compile(stylesheet.asSource()).load();

			transformer.setSource(sourceDocument.asSource());
			// TODO transformer.setMessageListener();
			final XdmDestination result = new XdmDestination();
			result.setBaseURI(outputBaseUri);
			transformer.setDestination(result);
			transformer.getUnderlyingController().setBaseOutputURI(outputBaseUri.toString());

			transformer.getUnderlyingController().setOutputURIResolver(new OutputURIResolver()
			{
				final Map<URI, XdmDestination> destinations = CollectionUtil.newSmallWriteOnceMap();


				public void close(final Result result) throws TransformerException
				{
					final URI uri = URI.create(result.getSystemId());
					assert destinations.containsKey(uri);
					final XdmDestination xdmResult = destinations.get(uri);
					log.trace("result base URI = %s", xdmResult.getXdmNode().getBaseURI());
					writeNodes(PORT_SECONDARY, environment, xdmResult.getXdmNode());
				}


				public Result resolve(final String href, final String base) throws TransformerException
				{
					final URI uri = UriUtil.resolve(href, base);
					assert uri != null;
					log.debug("new result document: %s", uri);

					try
					{
						final XdmDestination xdmResult = new XdmDestination();
						xdmResult.setBaseURI(uri);
						destinations.put(uri, xdmResult);
						final Receiver receiver = xdmResult.getReceiver(processor.getUnderlyingConfiguration());
						receiver.setSystemId(uri.toString());

						return receiver;
					}
					catch (final SaxonApiException e)
					{
						throw new PipelineException(e);
					}
				}
			});

			final String initialMode = getVariable(OPTION_INITIAL_MODE, environment, null);
			if (initialMode != null)
			{
				// FIXME does not handle namespaces
				transformer.setInitialMode(new QName(initialMode));
			}

			final Map<QName, String> parameters = readParameters(PORT_PARAMETERS, environment);
			log.debug("parameters = %s", parameters);
			for (final Map.Entry<QName, String> parameter : parameters.entrySet())
			{
				transformer.setParameter(parameter.getKey(), new XdmAtomicValue(parameter.getValue()));
			}

			transformer.transform();

			writeNodes(PORT_RESULT, environment, result.getXdmNode());
		}
		catch (final Exception e)
		{
			throw new PipelineException(e);
		}
	}
}
