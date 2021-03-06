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
package org.trancecode.function;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Collection;

/**
 * Utility methods related to {@link Predicate}.
 * 
 * @author Herve Quiroz
 */
public final class TcPredicates
{
    private TcPredicates()
    {
        // No instantiation
    }

    public static <T> Predicate<T> matches(final Collection<T> filters)
    {
        if (filters.isEmpty())
        {
            return Predicates.alwaysTrue();
        }

        return isContainedBy(filters);
    }

    public static <T> Predicate<T> isContainedBy(final Collection<T> collection)
    {
        return new IsContainedByPredicate<>(collection);
    }

    private static class IsContainedByPredicate<T> implements Predicate<T>
    {
        private final Collection<T> collections;

        public IsContainedByPredicate(final Collection<T> collection)
        {
            this.collections = Preconditions.checkNotNull(collection);
        }

        @Override
        public boolean apply(final T object)
        {
            return collections.contains(object);
        }
    }

    public static <T> Predicate<T> asPredicate(final Function<T, Boolean> function)
    {
        return new FunctionAsPredicate<>(function);
    }

    private static class FunctionAsPredicate<T> implements Predicate<T>
    {
        private final Function<T, Boolean> function;

        public FunctionAsPredicate(final Function<T, Boolean> function)
        {
            super();
            this.function = Preconditions.checkNotNull(function);
        }

        @Override
        public boolean apply(final T input)
        {
            return function.apply(input);
        }
    }

    public static <T> Predicate<T> identicalTo(final T target)
    {
        return new IdenticalToPredicate<>(target);
    }

    private static final class IdenticalToPredicate<T> implements Predicate<T>
    {
        private final T target;

        public IdenticalToPredicate(final T target)
        {
            this.target = Preconditions.checkNotNull(target);
        }

        @Override
        public boolean apply(final T input)
        {
            return input == target;
        }
    }
}
