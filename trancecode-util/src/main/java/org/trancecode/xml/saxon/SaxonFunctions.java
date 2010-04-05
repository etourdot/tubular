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
package org.trancecode.xml.saxon;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * Utility methods related to {@link Function} and Saxon.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class SaxonFunctions
{
    private SaxonFunctions()
    {
        // No instantiation
    }

    public static Function<XdmNode, XdmNodeKind> getNodeKind()
    {
        return GetNodeKindFunction.INSTANCE;
    }

    private static class GetNodeKindFunction implements Function<XdmNode, XdmNodeKind>
    {
        private static GetNodeKindFunction INSTANCE = new GetNodeKindFunction();

        private GetNodeKindFunction()
        {
            // Singleton
        }

        @Override
        public XdmNodeKind apply(final XdmNode node)
        {
            return node.getNodeKind();
        }
    }

    public static Function<XdmNode, Iterator<XdmItem>> axisIterator(final Axis axis)
    {
        return new AxisIteratorFunction(axis);
    }

    private static class AxisIteratorFunction implements Function<XdmNode, Iterator<XdmItem>>
    {
        private final Axis axis;

        public AxisIteratorFunction(final Axis axis)
        {
            super();
            Preconditions.checkNotNull(axis);
            this.axis = axis;
        }

        @Override
        public Iterator<XdmItem> apply(final XdmNode node)
        {
            return node.axisIterator(axis);
        }
    }

    public static Function<XdmNode, String> getStringValue()
    {
        return GetStringValueFunction.INSTANCE;
    }

    private static class GetStringValueFunction implements Function<XdmNode, String>
    {
        private static GetStringValueFunction INSTANCE = new GetStringValueFunction();

        private GetStringValueFunction()
        {
            // Singleton
        }

        @Override
        public String apply(final XdmNode node)
        {
            return node.getStringValue();
        }
    }

    public static Function<XdmNode, QName> getNodeName()
    {
        return GetNodeNameFunction.INSTANCE;
    }

    private static class GetNodeNameFunction implements Function<XdmNode, QName>
    {
        private static GetNodeNameFunction INSTANCE = new GetNodeNameFunction();

        private GetNodeNameFunction()
        {
            // Singleton
        }

        @Override
        public QName apply(final XdmNode node)
        {
            return node.getNodeName();
        }
    }
}
