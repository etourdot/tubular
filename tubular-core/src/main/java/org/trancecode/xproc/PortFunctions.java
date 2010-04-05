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
package org.trancecode.xproc;

import com.google.common.base.Function;

/**
 * {@link Function} implementations related to {@link Port}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
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

    private static class GetPortNameFunction implements Function<Port, String>
    {
        public static final GetPortNameFunction INSTANCE = new GetPortNameFunction();

        private GetPortNameFunction()
        {
            // Singleton
        }

        @Override
        public String apply(final Port port)
        {
            return port.getPortName();
        }
    }

    public static Function<HasPortReference, PortReference> getPortReference()
    {
        return GetPortReferenceFunction.INSTANCE;
    }

    private static class GetPortReferenceFunction implements Function<HasPortReference, PortReference>
    {
        public static final GetPortReferenceFunction INSTANCE = new GetPortReferenceFunction();

        private GetPortReferenceFunction()
        {
            // Singleton
        }

        @Override
        public PortReference apply(final HasPortReference port)
        {
            return port.getPortReference();
        }
    }
}
