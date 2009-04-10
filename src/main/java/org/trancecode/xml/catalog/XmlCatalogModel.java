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

import org.trancecode.xml.Namespace;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XmlCatalogModel
{
	Namespace NAMESPACE = new Namespace("urn:oasis:names:tc:entity:xmlns:xml:catalog", "catalog");

	QName ELEMENT_CATALOG = NAMESPACE.newSaxonQName("catalog");
	QName ELEMENT_GROUP = NAMESPACE.newSaxonQName("group");
	QName ELEMENT_REWRITE_SYSTEM = NAMESPACE.newSaxonQName("rewriteSystem");
	QName ELEMENT_REWRITE_URI = NAMESPACE.newSaxonQName("rewriteURI");

	Set<QName> ELEMENTS_CATALOG = ImmutableSet.of(ELEMENT_REWRITE_SYSTEM, ELEMENT_REWRITE_URI, ELEMENT_GROUP);
	Set<QName> ELEMENTS_GROUP = ImmutableSet.of(ELEMENT_CATALOG, ELEMENT_GROUP);

	QName ATTRIBUTE_SYSTEM_ID_START_STRING = new QName("systemIdStartString");
	QName ATTRIBUTE_REWRITE_PREFIX = new QName("rewritePrefix");
	QName ATTRIBUTE_URI_START_STRING = new QName("uriStartString");
}
