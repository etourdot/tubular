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
package org.trancecode.xml;

import org.trancecode.io.IOUtil;
import org.trancecode.io.InputResolver;
import org.trancecode.xml.catalog.Catalog;

import java.io.IOException;
import java.net.URI;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Utility methods related to {@link EntityResolver}.
 * 
 * @author Herve Quiroz
 * @version $Revision: 3842 $
 */
public final class EntityResolvers
{
	private EntityResolvers()
	{
		// No instantiation
	}


	public static EntityResolver newEntityResolver(final InputResolver inputResolver)
	{
		return newEntityResolver(inputResolver, Catalog.defaultCatalog());
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
			super();
			Preconditions.checkNotNull(inputResolver);
			Preconditions.checkNotNull(catalog);
			this.inputResolver = inputResolver;
			this.catalog = catalog;
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


	public static EntityResolver nullEntityResolver()
	{
		return NullEntityResolver.INSTANCE;
	}


	private static class NullEntityResolver implements EntityResolver
	{
		public static final NullEntityResolver INSTANCE = new NullEntityResolver();

		private static final Logger LOG = LoggerFactory.getLogger(NullEntityResolver.class);


		private NullEntityResolver()
		{
			// Singleton
		}


		public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException
		{
			LOG.trace("publicId = {} ; systemId = {}", publicId, systemId);

			final InputSource inputSource = new InputSource(IOUtil.newNullInputStream());
			inputSource.setPublicId(publicId);
			inputSource.setSystemId(systemId);

			return inputSource;
		}
	}
}