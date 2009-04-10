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
package org.trancecode.xml.catalog;

import org.trancecode.annotation.Nullable;
import org.trancecode.annotation.ReturnsNullable;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractCatalog implements Catalog
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractCatalog.class);

	protected final URI baseUri;


	protected AbstractCatalog(final URI baseUri)
	{
		this.baseUri = baseUri;
	}


	@Override
	@ReturnsNullable
	public URI resolve(
		@Nullable final String publicId, @Nullable final String systemId, @Nullable final String href,
		@Nullable final String base)
	{
		LOG.trace("publicId = {} ; systemId = {} ; href = {} ; base = {} ; baseUri = {}", new Object[] { publicId,
			systemId, href, base, baseUri });

		final URI resolvedUri = doResolve(publicId, systemId, href, base);
		LOG.trace("resolvedUri = {}", resolvedUri);

		final URI resultUri;
		if (resolvedUri == null)
		{
			resultUri = null;
		}
		else if (baseUri == null)
		{
			resultUri = resolvedUri;
		}
		else
		{
			resultUri = baseUri.resolve(resolvedUri);
		}

		LOG.trace("resultUri = {}", resultUri);
		return resultUri;
	}


	@ReturnsNullable
	protected abstract URI doResolve(
		@Nullable final String publicId, @Nullable final String systemId, @Nullable final String href,
		@Nullable final String base);
}
