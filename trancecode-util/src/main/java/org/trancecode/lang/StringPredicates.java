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
package org.trancecode.lang;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * {@link Predicate} implementations related to {@link String}.
 * 
 * @author Herve Quiroz
 */
public final class StringPredicates
{
    /**
     * @see String#endsWith(String)
     */
    public static Predicate<String> endsWith(final String suffix)
    {
        return new Predicate<String>()
        {
            @Override
            public boolean apply(final String string)
            {
                return string.endsWith(suffix);
            }
        };
    }

    public static Predicate<String> isEmpty()
    {
        // TODO singleton
        return new Predicate<String>()
        {
            @Override
            public boolean apply(final String string)
            {
                return string.isEmpty();
            }
        };
    }

    public static Predicate<String> isNotEmpty()
    {
        // TODO singleton
        return Predicates.not(isEmpty());
    }

    private StringPredicates()
    {
        // No instantiation
    }
}
