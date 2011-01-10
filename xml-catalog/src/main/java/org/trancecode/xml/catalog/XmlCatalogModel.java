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

import com.google.common.collect.ImmutableSet;

import java.util.Set;

import net.sf.saxon.s9api.QName;

/**
 * @author Herve Quiroz
 */
public final class XmlCatalogModel
{
    public static final class XmlCatalogElements
    {
        public static final QName CATALOG = XmlCatalogNamespace.INSTANCE.newSaxonQName("catalog");
        public static final QName GROUP = XmlCatalogNamespace.INSTANCE.newSaxonQName("group");
        public static final QName REWRITE_SYSTEM = XmlCatalogNamespace.INSTANCE.newSaxonQName("rewriteSystem");
        public static final QName REWRITE_URI = XmlCatalogNamespace.INSTANCE.newSaxonQName("rewriteURI");

        public static final Set<QName> ELEMENTS_CATALOG = ImmutableSet.of(REWRITE_SYSTEM, REWRITE_URI, GROUP);
        public static final Set<QName> ELEMENTS_GROUP = ImmutableSet.of(CATALOG, GROUP);

        private XmlCatalogElements()
        {
            // No instantiation
        }
    }

    public static final class XmlCatalogAttributes
    {
        public static final QName SYSTEM_ID_START_STRING = new QName("systemIdStartString");
        public static final QName REWRITE_PREFIX = new QName("rewritePrefix");
        public static final QName URI_START_STRING = new QName("uriStartString");

        private XmlCatalogAttributes()
        {
            // No instantiation
        }
    }

    private XmlCatalogModel()
    {
        // No instantiation
    }
}
