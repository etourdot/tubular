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

import org.trancecode.xml.catalog.CatalogException;
import org.trancecode.xml.catalog.XmlCatalogModel;
import org.trancecode.xml.saxon.SaxonUtil;

import java.net.URI;

import javax.xml.transform.Source;

import com.google.common.base.Function;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class CatalogParser
{
	private static final Logger LOG = LoggerFactory.getLogger(CatalogParser.class);

	private final Processor processor;


	public CatalogParser(final Processor processor)
	{
		assert processor != null;
		this.processor = processor;
	}


	public Function<CatalogQuery, URI> parse(final Source source)
	{
		try
		{
			return doParse(source);
		}
		catch (final SaxonApiException e)
		{
			throw new CatalogException(e, "error while parsing catalog from source: %s", source);
		}
	}


	private Function<CatalogQuery, URI> doParse(final Source source) throws SaxonApiException
	{
		assert source != null;

		final DocumentBuilder documentBuilder = processor.newDocumentBuilder();
		documentBuilder.setLineNumbering(true);
		final XdmNode document = documentBuilder.build(source);
		final XdmNode catalogNode = SaxonUtil.childElement(document, XmlCatalogModel.ELEMENT_CATALOG);

		return parse(catalogNode);
	}


	private Function<CatalogQuery, URI> parse(final XdmNode catalogNode) throws SaxonApiException
	{
		final Function<CatalogQuery, URI> catalog = null;
		LOG.trace("new catalog = {}", catalog);
		return catalog;
	}
}
