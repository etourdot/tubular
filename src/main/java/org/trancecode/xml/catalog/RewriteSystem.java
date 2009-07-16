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
import org.trancecode.core.ObjectUtil;
import org.trancecode.io.Uris;

import java.net.URI;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class RewriteSystem extends AbstractCatalog
{
	private final String systemIdStartString;
	private final String rewritePrefix;


	public RewriteSystem(final String systemIdStartString, final String rewritePrefix, final URI baseUri)
	{
		super(baseUri);

		assert systemIdStartString != null;
		this.systemIdStartString = systemIdStartString;

		assert rewritePrefix != null;
		this.rewritePrefix = rewritePrefix;
	}


	@Override
	@ReturnsNullable
	protected URI doResolve(
		@Nullable final String publicId, @Nullable final String systemId, @Nullable final String href,
		@Nullable final String base)
	{
		if (systemId != null && systemId.startsWith(systemIdStartString))
		{
			final String suffix = systemId.substring(systemIdStartString.length());
			return Uris.createUri(rewritePrefix + suffix);
		}

		return null;
	}


	@Override
	public int hashCode()
	{
		return ObjectUtil.hashCode(RewriteUri.class, baseUri, systemIdStartString, rewritePrefix);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o != null && o instanceof RewriteUri)
		{
			final RewriteSystem rewriteSystem = (RewriteSystem)o;
			return ObjectUtil.pairEquals(
				baseUri, rewriteSystem.baseUri, systemIdStartString, rewriteSystem.systemIdStartString, rewritePrefix,
				rewriteSystem.rewritePrefix);
		}

		return false;
	}
}
