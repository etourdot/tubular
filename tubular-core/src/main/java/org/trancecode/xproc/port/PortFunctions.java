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
package org.trancecode.xproc.port;

import com.google.common.base.Function;
import org.trancecode.xproc.binding.PortBinding;

/**
 * {@link Function} implementations related to {@link Port}.
 * 
 * @author Herve Quiroz
 */
public final class PortFunctions
{
    private PortFunctions()
    {
        // No instantiation
    }

    public static Function<Port, String> getPortName()
    {
        return GetPortNameFunction.INSTANCE;
    }

    private static final class GetPortNameFunction implements Function<Port, String>
    {
        public static final GetPortNameFunction INSTANCE = new GetPortNameFunction();

        @Override
        public String apply(final Port port)
        {
            return port.getPortName();
        }
    }

    public static Function<HasPortReference, PortReference> getPortReference()
    {
        return PortReferenceFunction.INSTANCE;
    }

    private static final class PortReferenceFunction implements Function<HasPortReference, PortReference>
    {
        public static final PortReferenceFunction INSTANCE = new PortReferenceFunction();

        @Override
        public PortReference apply(final HasPortReference port)
        {
            return port.getPortReference();
        }
    }

    public static Function<Port, Iterable<PortBinding>> getPortBindings()
    {
        return GetPortBindingsFunction.INSTANCE;
    }

    private static final class GetPortBindingsFunction implements Function<Port, Iterable<PortBinding>>
    {
        public static final GetPortBindingsFunction INSTANCE = new GetPortBindingsFunction();

        @Override
        public Iterable<PortBinding> apply(final Port port)
        {
            return port.getPortBindings();
        }
    }
}
