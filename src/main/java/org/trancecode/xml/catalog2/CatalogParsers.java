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

import org.trancecode.core.AbstractImmutableObject;
import org.trancecode.core.collection.TubularMaps;
import org.trancecode.xml.catalog.XmlCatalogModel;
import org.trancecode.xml.saxon.SaxonUtil;

import java.net.URI;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class CatalogParsers
{
	private CatalogParsers()
	{
		// No instantiation
	}


	public static Function<XdmNode, Function<CatalogQuery, URI>> rewriteSystem()
	{
		return RewriteSystemParser.INSTANCE;
	}


	private static class RewriteSystemParser implements Function<XdmNode, Function<CatalogQuery, URI>>
	{
		private static final RewriteSystemParser INSTANCE = new RewriteSystemParser();


		private RewriteSystemParser()
		{
			// Singleton
		}


		@Override
		public Function<CatalogQuery, URI> apply(final XdmNode node)
		{
			Preconditions.checkNotNull(node);
			final String systemIdStartString = node.getAttributeValue(XmlCatalogModel.ATTRIBUTE_SYSTEM_ID_START_STRING);
			final String rewritePrefix = node.getAttributeValue(XmlCatalogModel.ATTRIBUTE_REWRITE_PREFIX);

			return Catalogs.rewriteSystem(systemIdStartString, rewritePrefix);
		}
	}


	public static Function<XdmNode, Function<CatalogQuery, URI>> rewriteUri()
	{
		return RewriteUriParser.INSTANCE;
	}


	private static class RewriteUriParser implements Function<XdmNode, Function<CatalogQuery, URI>>
	{
		private static final RewriteUriParser INSTANCE = new RewriteUriParser();


		private RewriteUriParser()
		{
			// Singleton
		}


		@Override
		public Function<CatalogQuery, URI> apply(final XdmNode node)
		{
			Preconditions.checkNotNull(node);
			final String uriStartString = node.getAttributeValue(XmlCatalogModel.ATTRIBUTE_URI_START_STRING);
			final String rewritePrefix = node.getAttributeValue(XmlCatalogModel.ATTRIBUTE_REWRITE_PREFIX);

			return Catalogs.rewriteUri(uriStartString, rewritePrefix);
		}
	}


	public static Function<XdmNode, Function<CatalogQuery, URI>> group(
		final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers)
	{
		return new GroupParser(parsers);
	}


	private static class GroupParser extends AbstractImmutableObject
		implements Function<XdmNode, Function<CatalogQuery, URI>>
	{
		private final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers;


		public GroupParser(final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers)
		{
			super();
			Preconditions.checkNotNull(parsers);

			final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> groupParsers =
				ImmutableMap.of(
					XmlCatalogModel.ELEMENT_CATALOG, (Function<XdmNode, Function<CatalogQuery, URI>>)this,
					XmlCatalogModel.ELEMENT_GROUP, this);
			this.parsers = TubularMaps.merge(parsers, groupParsers);
		}


		@Override
		public Function<CatalogQuery, URI> apply(final XdmNode node)
		{
			Preconditions.checkNotNull(node);

			return Catalogs.routingCatalog(ImmutableList.copyOf(Iterables.transform(SaxonUtil.childElements(
				node, XmlCatalogModel.ELEMENTS_CATALOG), routingParser(parsers))));
		}
	}


	private static Function<XdmNode, Function<CatalogQuery, URI>> routingParser(
		final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers)
	{
		return new RoutingParser(parsers);
	}


	private static class RoutingParser extends AbstractImmutableObject
		implements Function<XdmNode, Function<CatalogQuery, URI>>
	{
		private final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers;


		public RoutingParser(final Map<QName, Function<XdmNode, Function<CatalogQuery, URI>>> parsers)
		{
			super();
			Preconditions.checkNotNull(parsers);
			this.parsers = parsers;
		}


		@Override
		public Function<CatalogQuery, URI> apply(final XdmNode node)
		{
			return parsers.get(node.getNodeName()).apply(node);
		}
	}


	private static URI getBaseUri(final XdmNode node)
	{
		return node.getBaseURI();
	}
}
