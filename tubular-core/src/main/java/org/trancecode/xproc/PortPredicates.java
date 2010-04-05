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

import com.google.common.base.Predicate;

/**
 * {@link Predicate} implementations related to {@link Port}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class PortPredicates
{
    private PortPredicates()
    {
        // No instantiation
    }

    public static Predicate<Port> isInputPort()
    {
        return IsInputPortPredicate.INSTANCE;
    }

    private static class IsInputPortPredicate implements Predicate<Port>
    {
        public static final IsInputPortPredicate INSTANCE = new IsInputPortPredicate();

        private IsInputPortPredicate()
        {
            // Singleton
        }

        @Override
        public boolean apply(final Port port)
        {
            return port.isInput();
        }
    }

    public static Predicate<Port> isOutputPort()
    {
        return IsOutputPortPredicate.INSTANCE;
    }

    private static class IsOutputPortPredicate implements Predicate<Port>
    {
        public static final IsOutputPortPredicate INSTANCE = new IsOutputPortPredicate();

        private IsOutputPortPredicate()
        {
            // Singleton
        }

        @Override
        public boolean apply(final Port port)
        {
            return port.isOutput();
        }
    }

    public static Predicate<Port> isParameterPort()
    {
        return IsParameterPortPredicate.INSTANCE;
    }

    private static class IsParameterPortPredicate implements Predicate<Port>
    {
        public static final IsParameterPortPredicate INSTANCE = new IsParameterPortPredicate();

        private IsParameterPortPredicate()
        {
            // Singleton
        }

        @Override
        public boolean apply(final Port port)
        {
            return port.isParameter();
        }
    }
}
