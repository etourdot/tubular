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

import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.trancecode.io.InputResolver;
import org.trancecode.xml.Sax;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Herve Quiroz
 */
public final class CatalogUriResolvers
{
    private CatalogUriResolvers()
    {
        // No instantiation
    }

    public static URIResolver newUriResolver(final InputResolver inputResolver, final Catalog catalog)
    {
        return new EntityResolverURIResolver(inputResolver, catalog);
    }

    private static class EntityResolverURIResolver implements URIResolver
    {
        private final InputResolver inputResolver;
        private final Catalog catalog;
        private final EntityResolver entityResolver;

        public EntityResolverURIResolver(final InputResolver inputResolver, final Catalog catalog)
        {
            this.inputResolver = Preconditions.checkNotNull(inputResolver);
            this.catalog = Preconditions.checkNotNull(catalog);
            entityResolver = CatalogEntityResolvers.newEntityResolver(inputResolver, catalog);
        }

        @Override
        public Source resolve(final String href, final String base) throws TransformerException
        {
            final URI uri = catalog.resolveUri(href, base);
            final InputSource inputSource = new InputSource(inputResolver.resolveInputStream(uri));
            inputSource.setSystemId(uri.toString());

            try
            {
                final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setEntityResolver(entityResolver);

                final SAXSource source = new SAXSource(xmlReader, inputSource);
                source.setSystemId(uri.toString());

                return source;
            }
            catch (final SAXException e)
            {
                Sax.closeQuietly(inputSource);
                throw new TransformerException(String.format("href = %s ; base = %s", href, base), e);
            }
        }
    }
}
