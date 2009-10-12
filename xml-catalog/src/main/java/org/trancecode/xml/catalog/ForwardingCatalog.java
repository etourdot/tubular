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

import java.net.URI;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class ForwardingCatalog extends ForwardingObject implements Function<CatalogQuery, URI>
{
	private final Function<CatalogQuery, URI> delegate;


	public ForwardingCatalog(final Function<CatalogQuery, URI> delegate)
	{
		super();
		Preconditions.checkNotNull(delegate);
		this.delegate = delegate;
	}


	@Override
	protected Function<CatalogQuery, URI> delegate()
	{
		return delegate;
	}


	@Override
	public URI apply(final CatalogQuery query)
	{
		return delegate.apply(query);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (o != null)
		{
			if (o instanceof ForwardingCatalog)
			{
				return o.equals(delegate);
			}

			return delegate.equals(o);
		}

		return false;
	}


	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}
}
