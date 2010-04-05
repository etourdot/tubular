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

import org.trancecode.io.Uris;
import org.trancecode.logging.Logger;
import org.trancecode.xml.XmlAttributes;
import org.trancecode.xml.saxon.SaxonUtil;

import java.net.URI;
import java.util.List;

import javax.xml.transform.Source;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * @author Herve Quiroz
 */
public class CatalogParser
{
    private static final Logger LOG = Logger.getLogger(CatalogParser.class);

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

    private Function<CatalogQuery, URI> parse(final XdmNode catalogNode)
    {
        final Function<CatalogQuery, URI> catalog = doParse(catalogNode);
        LOG.trace("new catalog = {}", catalog);
        return catalog;
    }

    private Function<CatalogQuery, URI> doParse(final XdmNode catalogNode)
    {
        assert catalogNode != null;

        final URI baseUri = Uris.createUri(catalogNode.getAttributeValue(XmlAttributes.BASE));

        if (XmlCatalogModel.ELEMENTS_GROUP.contains(catalogNode.getNodeName()))
        {
            return parseGroup(catalogNode, baseUri);
        }

        if (catalogNode.getNodeName().equals(XmlCatalogModel.ELEMENT_REWRITE_SYSTEM))
        {
            return parseRewriteSystem(catalogNode, baseUri);
        }

        if (catalogNode.getNodeName().equals(XmlCatalogModel.ELEMENT_REWRITE_URI))
        {
            return parseRewriteUri(catalogNode, baseUri);
        }

        throw new CatalogException("unsupported catalog element: %s", catalogNode.getNodeName());
    }

    private Function<CatalogQuery, URI> parseGroup(final XdmNode catalogNode, final URI baseUri)
    {
        assert catalogNode != null;

        final List<Function<CatalogQuery, URI>> catalogs = ImmutableList.copyOf(Iterables.transform(SaxonUtil
                .childElements(catalogNode, XmlCatalogModel.ELEMENTS_CATALOG),
                new Function<XdmNode, Function<CatalogQuery, URI>>()
                {
                    @Override
                    public Function<CatalogQuery, URI> apply(final XdmNode node)
                    {
                        return doParse(node);
                    }
                }));

        return Catalogs.group(baseUri, catalogs);
    }

    private Function<CatalogQuery, URI> parseRewriteSystem(final XdmNode catalogNode, final URI baseUri)

    {
        assert catalogNode != null;

        final String systemIdStartString = catalogNode
                .getAttributeValue(XmlCatalogModel.ATTRIBUTE_SYSTEM_ID_START_STRING);
        final String rewritePrefix = catalogNode.getAttributeValue(XmlCatalogModel.ATTRIBUTE_REWRITE_PREFIX);

        return Catalogs.setBaseUri(baseUri, Catalogs.rewriteSystem(systemIdStartString, rewritePrefix));
    }

    private Function<CatalogQuery, URI> parseRewriteUri(final XdmNode catalogNode, final URI baseUri)

    {
        assert catalogNode != null;

        final String uriStartString = catalogNode.getAttributeValue(XmlCatalogModel.ATTRIBUTE_URI_START_STRING);
        final String rewritePrefix = catalogNode.getAttributeValue(XmlCatalogModel.ATTRIBUTE_REWRITE_PREFIX);

        return Catalogs.setBaseUri(baseUri, Catalogs.rewriteUri(uriStartString, rewritePrefix));
    }
}
