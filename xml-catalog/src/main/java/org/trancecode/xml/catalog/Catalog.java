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
import org.trancecode.io.Uris;

import java.net.URI;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class Catalog extends ForwardingCatalog
{
    private static final Catalog defaultCatalog = newCatalog(Catalogs.defaultCatalog());

    public static Catalog defaultCatalog()
    {
        return defaultCatalog;
    }

    public static Catalog newCatalog(final Function<CatalogQuery, URI> catalogFunction)
    {
        return new Catalog(catalogFunction);
    }

    private Catalog(final Function<CatalogQuery, URI> catalog)
    {
        super(catalog);
    }

    @ReturnsNullable
    public URI resolveEntity(@Nullable final String publicId, @Nullable final String systemId)
    {
        return apply(CatalogQuery.newInstance(publicId, systemId, null));
    }

    @ReturnsNullable
    public URI resolveUri(@Nullable final String href, @Nullable final String base)
    {
        Preconditions.checkArgument(href != null || base != null);
        return apply(CatalogQuery.newInstance(null, null, Uris.resolve(href, base)));
    }

    @ReturnsNullable
    public URI resolveUri(@Nullable final URI uri)
    {
        return apply(CatalogQuery.newInstance(null, null, uri));
    }
}
