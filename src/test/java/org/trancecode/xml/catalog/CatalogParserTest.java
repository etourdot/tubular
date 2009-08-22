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

import org.trancecode.AbstractTest;
import org.trancecode.io.Paths;

import java.io.InputStream;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link CatalogParser}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public class CatalogParserTest extends AbstractTest
{
	private Source getSourceFromClasspath(final String path)
	{
		final InputStream inputStream = getClass().getResourceAsStream(Paths.asAbsolutePath(path));
		return new StreamSource(inputStream);
	}


	private Source getSourceFromTestResources(final String path)
	{
		return getSourceFromClasspath(getClass().getSimpleName() + Paths.asAbsolutePath(path));
	}


	private Processor getProcessor()
	{
		return new Processor(false);
	}


	@Test
	public void parse()
	{
		final Source catalogSource = getSourceFromTestResources("catalog.xml");
		final Catalog catalog = Catalog.newCatalog(new CatalogParser(getProcessor()).parse(catalogSource));

		Assert.assertEquals(URI.create("some/rewriten/path/whatever"), catalog.resolveUri("some/path/whatever", null));
	}
}
