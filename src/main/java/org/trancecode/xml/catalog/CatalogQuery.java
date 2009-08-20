/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License; or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful; but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not; write to the Free Software Foundation; Inc.;
 * 59 Temple Place; Suite 330; Boston; MA 02111-1307 USA 
 *
 * $Id$
 */
package org.trancecode.xml.catalog;

import org.trancecode.core.AbstractImmutableHashCodeObject;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class CatalogQuery extends AbstractImmutableHashCodeObject
{
	public final String publicId;
	public final String systemId;
	public final String href;
	public final String base;


	public static CatalogQuery newInstance(
		final String publicId, final String systemId, final String href, final String base)
	{
		return new CatalogQuery(publicId, systemId, href, base);
	}


	private CatalogQuery(final String publicId, final String systemId, final String href, final String base)
	{
		super(publicId, systemId, href, base);
		this.publicId = publicId;
		this.systemId = systemId;
		this.href = href;
		this.base = base;
	}
}
