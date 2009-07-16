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
package org.trancecode.xml.catalog2;

import org.trancecode.annotation.Nullable;
import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.core.AbstractImmutableObject;
import org.trancecode.core.function.TubularFunctions;

import java.net.URI;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class Catalog extends AbstractImmutableObject implements Function<CatalogQuery, URI>
{
	private final Iterable<Function<CatalogQuery, URI>> catalogEntries;


	public static Catalog newCatalog(final Iterable<Function<CatalogQuery, URI>> catalogEntries)
	{
		return new Catalog(ImmutableList.copyOf(catalogEntries));
	}


	public static Catalog newCatalog(final Function<CatalogQuery, URI>... catalogEntries)
	{
		return newCatalog(ImmutableList.of(catalogEntries));
	}


	private static Catalog newCatalog(
		final Iterable<Function<CatalogQuery, URI>> catalogEntries1,
		final Iterable<Function<CatalogQuery, URI>> catalogEntries2)
	{
		return newCatalog(Iterables.concat(catalogEntries1, catalogEntries2));
	}


	private Catalog(final Iterable<Function<CatalogQuery, URI>> catalogEntries)
	{
		super(catalogEntries);
		this.catalogEntries = catalogEntries;
	}


	@ReturnsNullable
	public URI resolve(
		@Nullable final String publicId, @Nullable final String systemId, @Nullable final String href,
		@Nullable final String base)
	{
		return apply(CatalogQuery.newInstance(publicId, systemId, href, base));
	}


	@Override
	public URI apply(final CatalogQuery query)
	{
		final Function<Function<CatalogQuery, URI>, URI> applyFunction = TubularFunctions.applyTo(query);
		final Iterable<URI> results = Iterables.transform(catalogEntries, applyFunction);
		return Iterables.find(results, Predicates.notNull());
	}


	public Catalog override(final Function<CatalogQuery, URI>... newCatalogEntries)
	{
		return override(ImmutableList.of(newCatalogEntries));
	}


	public Catalog override(final Iterable<Function<CatalogQuery, URI>> newCatalogEntries)
	{
		return newCatalog(newCatalogEntries, catalogEntries);
	}


	public Catalog addEntries(final Function<CatalogQuery, URI>... newCatalogEntries)
	{
		return addEntries(ImmutableList.of(newCatalogEntries));
	}


	public Catalog addEntries(final Iterable<Function<CatalogQuery, URI>> newCatalogEntries)
	{
		return newCatalog(catalogEntries, newCatalogEntries);
	}
}
