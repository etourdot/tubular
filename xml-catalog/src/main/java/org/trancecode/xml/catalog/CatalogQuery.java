/*
 * Copyright (C) 2008 Herve Quiroz
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

import com.google.common.base.Preconditions;

import java.net.URI;

/**
 * @author Herve Quiroz
 */
public final class CatalogQuery
{
    private final String publicId;
    private final String systemId;
    private final URI uri;
    private String uriAsString;

    public static CatalogQuery newInstance(final String publicId, final String systemId, final URI uri)
    {
        return new CatalogQuery(publicId, systemId, uri);
    }

    private CatalogQuery(final String publicId, final String systemId, final URI uri)
    {
        Preconditions.checkArgument(publicId != null || systemId != null || uri != null);
        this.publicId = publicId;
        this.systemId = systemId;
        this.uri = uri;
    }

    public String publicId()
    {
        return publicId;
    }

    public String systemId()
    {
        return systemId;
    }

    public URI uri()
    {
        return uri;
    }

    public String uriAsString()
    {
        if (uriAsString == null && uri != null)
        {
            uriAsString = uri.toString();
        }

        return uriAsString;
    }
}
