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

import java.io.IOException;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class CatalogAwareResolver implements URIResolver, EntityResolver, XMLResolver
{

	@Override
	public Source resolve(final String href, final String base) throws TransformerException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object resolveEntity(
		final String publicId, final String systemId, final String baseUri, final String namespace)
		throws XMLStreamException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
