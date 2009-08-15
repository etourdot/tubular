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
import org.trancecode.core.TubularObjects;

import java.net.URI;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Group extends AbstractCatalog
{
	private static final Logger LOG = LoggerFactory.getLogger(Group.class);

	private final Iterable<Catalog> catalogs;


	public Group(final Iterable<Catalog> catalogs, final URI baseUri)
	{
		super(baseUri);

		this.catalogs = ImmutableList.copyOf(catalogs);
	}


	@Override
	@ReturnsNullable
	protected URI doResolve(
		@Nullable final String publicId, @Nullable final String systemId, @Nullable final String href,
		@Nullable final String base)
	{
		LOG.trace("publicId = {} ; systemId = {} ; href = {} ; base = {}", new Object[] { publicId, systemId, href,
			base });

		for (final Catalog catalog : catalogs)
		{
			final URI resolvedUri = catalog.resolve(publicId, systemId, href, base);
			if (resolvedUri != null)
			{
				return resolvedUri;
			}
		}

		return null;
	}


	@Override
	public int hashCode()
	{
		return TubularObjects.hashCode(Group.class, baseUri, catalogs);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o != null && o instanceof Group)
		{
			final Group group = (Group)o;
			return TubularObjects.pairEquals(baseUri, group.baseUri, catalogs, group.catalogs);
		}

		return false;
	}
}
