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
package org.trancecode.xml.saxon;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Iterator;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.function.TcPredicates;

/**
 * Utility methods to retrieve node sequences through axis in Saxon.
 * 
 * @author Herve Quiroz
 */
public final class SaxonAxis
{
    private SaxonAxis()
    {
        // No instantiation
    }

    public static Iterable<XdmNode> attributes(final XdmNode node)
    {
        return Iterables.filter(childNodes(node), SaxonPredicates.isAttribute());
    }

    public static Iterable<XdmItem> axis(final XdmNode node, final Axis axis)
    {
        return () -> node.axisIterator(axis);
    }

    public static XdmNode childElement(final XdmNode node)
    {
        return Iterables.getOnlyElement(childElements(node), null);
    }

    public static XdmNode childElement(final XdmNode node, final Collection<QName> elementNames)
    {
        return Iterables.getOnlyElement(childElements(node, elementNames), null);
    }

    public static XdmNode childElement(final XdmNode node, final QName... elementNames)
    {
        return childElement(node, ImmutableSet.copyOf(elementNames));
    }

    public static Iterable<XdmNode> childElements(final XdmNode node)
    {
        return Iterables.filter(childNodes(node), SaxonPredicates.isElement());
    }

    public static Iterable<XdmNode> childElements(final XdmNode node, final Collection<QName> elementNames)
    {
        return Iterables.filter(childElements(node),
                Predicates.compose(TcPredicates.matches(elementNames), SaxonFunctions.getNodeName()));
    }

    public static Iterable<XdmNode> childElements(final XdmNode node, final QName... elementNames)
    {
        return childElements(node, ImmutableSet.copyOf(elementNames));
    }

    public static Iterable<XdmNode> childNodes(final XdmNode node)
    {
        return Iterables.filter(childXdmItems(node), XdmNode.class);
    }

    public static Iterable<XdmNode> childNodesNoAttributes(final XdmNode node)
    {
        return Iterables.filter(childNodes(node), Predicates.not(SaxonPredicates.isAttribute()));
    }

    public static Iterable<XdmItem> childXdmItems(final XdmNode node)
    {
        return Iterables.concat(axis(node, Axis.ATTRIBUTE), axis(node, Axis.CHILD));
    }

    public static Iterable<XdmNode> namespaces(final XdmNode node)
    {
        return Iterables.filter(axis(node, Axis.NAMESPACE), XdmNode.class);
    }
}
