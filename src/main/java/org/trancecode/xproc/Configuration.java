/*
 * Copyright (C) 2007 TranceCode Software
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

import org.trancecode.io.DefaultInputResolver;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Configuration
{
	public static final String PROPERTY_TEMPORARY_NODES_CACHING_ENABLED =
		Configuration.class.getName() + ".temporaryNodesCachingEnabled";

	private boolean temporaryNodesCachingEnabled = Boolean.parseBoolean(PROPERTY_TEMPORARY_NODES_CACHING_ENABLED);
	private URIResolver uriResolver;
	private OutputResolver outputResolver;
	private InputResolver inputResolver = DefaultInputResolver.INSTANCE;
	private final Processor processor;


	public Configuration(final Processor processor)
	{
		assert processor != null;
		this.processor = processor;

		uriResolver = processor.getUnderlyingConfiguration().getURIResolver();
	}


	public InputResolver getInputResolver()
	{
		return this.inputResolver;
	}


	public void setInputResolver(final InputResolver inputResolver)
	{
		this.inputResolver = inputResolver;
	}


	public boolean isTemporaryNodesCachingEnabled()
	{
		return temporaryNodesCachingEnabled;
	}


	public void setTemporaryNodesCachingEnabled(final boolean temporaryNodesCachingEnabled)
	{
		this.temporaryNodesCachingEnabled = temporaryNodesCachingEnabled;
	}


	public URIResolver getUriResolver()
	{
		return uriResolver;
	}


	public void setUriResolver(final URIResolver uriResolver)
	{
		this.uriResolver = uriResolver;
	}


	public OutputResolver getOutputResolver()
	{
		return this.outputResolver;
	}


	public void setOutputResolver(final OutputResolver outputResolver)
	{
		this.outputResolver = outputResolver;
	}


	public Processor getProcessor()
	{
		return processor;
	}
}
