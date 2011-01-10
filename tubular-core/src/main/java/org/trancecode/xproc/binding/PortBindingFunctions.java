/*
 * Copyright (C) 2010 Herve Quiroz
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
 */
package org.trancecode.xproc.binding;

import com.google.common.base.Function;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.xml.saxon.SaxonLocation;

/**
 * {@link Function} implementations related to {@link PortBinding}.
 * 
 * @author Herve Quiroz
 */
public final class PortBindingFunctions
{
    private PortBindingFunctions()
    {
        // No instantiation
    }

    public static Function<XdmNode, PortBinding> toPortBinding()
    {
        return ToPortBindingFunction.INSTANCE;
    }

    private static final class ToPortBindingFunction implements Function<XdmNode, PortBinding>
    {
        private static final ToPortBindingFunction INSTANCE = new ToPortBindingFunction();

        @Override
        public PortBinding apply(final XdmNode node)
        {
            return new InlinePortBinding(node, SaxonLocation.of(node));
        }
    }
}
