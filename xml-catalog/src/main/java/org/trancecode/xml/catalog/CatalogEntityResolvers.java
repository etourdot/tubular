/*
 * Copyright (C) 2008 Herve Quiroz
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

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.URI;

import org.trancecode.io.InputResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Herve Quiroz
 */
public final class CatalogEntityResolvers
{
    private CatalogEntityResolvers()
    {
        // No instantiation
    }

    public static EntityResolver newEntityResolver(final InputResolver inputResolver, final Catalog catalog)
    {
        return new InputResolverEntityResolver(inputResolver, catalog);
    }

    private static class InputResolverEntityResolver implements EntityResolver
    {
        private final InputResolver inputResolver;
        private final Catalog catalog;

        public InputResolverEntityResolver(final InputResolver inputResolver, final Catalog catalog)
        {
            this.inputResolver = Preconditions.checkNotNull(inputResolver);
            this.catalog = Preconditions.checkNotNull(catalog);
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException
        {
            final URI uri = catalog.resolveEntity(publicId, systemId);
            final InputSource inputSource = new InputSource(inputResolver.resolveInputStream(uri));
            inputSource.setSystemId(uri.toString());

            return inputSource;
        }
    }
}
