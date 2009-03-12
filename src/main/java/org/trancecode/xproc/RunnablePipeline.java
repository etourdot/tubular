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
package org.trancecode.xproc;

import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.binding.InlinePortBinding;

import java.util.Arrays;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class RunnablePipeline
{
	private URIResolver uriResolver;
	private OutputResolver outputResolver;
	private final Pipeline pipeline;
	private final XLogger log = XLoggerFactory.getXLogger(getClass());


	protected RunnablePipeline(final Pipeline pipeline)
	{
		assert pipeline != null;
		this.pipeline = pipeline;
	}


	public PipelineResult run()
	{
		log.entry();

		final Configuration configuration = new Configuration(pipeline.getProcessor());
		configuration.setOutputResolver(outputResolver);
		configuration.setUriResolver(uriResolver);
		final Environment environment = new Environment(getUnderlyingPipeline(), configuration);

		final Environment resultEnvironment = getUnderlyingPipeline().run(environment);

		return new PipelineResult(pipeline, resultEnvironment);
	}


	public void withParam(final QName name, final String value)
	{
		log.entry(name, value);
		getUnderlyingPipeline().withParam(name, null, value, getUnderlyingPipeline().getLocation());
	}


	public void withOption(final QName name, final String value)
	{
		log.entry(name, value);
		getUnderlyingPipeline().withOptionValue(name, value);
	}


	protected org.trancecode.xproc.step.Pipeline getUnderlyingPipeline()
	{
		return pipeline.getUnderlyingPipeline();
	}


	public void setPortBinding(final String portName, final Iterable<Source> bindings)
	{
		final Port port = getUnderlyingPipeline().getPort(portName);
		port.getPortBindings().clear();
		for (final Source binding : bindings)
		{
			try
			{
				final XdmNode node = getPipeline().getProcessor().newDocumentBuilder().build(binding);
				port.getPortBindings().add(new InlinePortBinding(node, null));
			}
			catch (final SaxonApiException e)
			{
				throw new PipelineException(e);
			}
		}
	}


	public void setPortBinding(final String portName, final Source... bindings)
	{
		setPortBinding(portName, Arrays.asList(bindings));
	}


	public void setUriResolver(final URIResolver uriResolver)
	{
		this.uriResolver = uriResolver;
	}


	public void setOutputResolver(final OutputResolver outputResolver)
	{
		this.outputResolver = outputResolver;
	}


	public Pipeline getPipeline()
	{
		return pipeline;
	}
}
