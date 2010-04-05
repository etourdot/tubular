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
package org.trancecode.collection;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.core.AbstractImmutableObject;
import org.trancecode.function.TranceCodeFunctions;

/**
 * Utility methods related to {@link Iterable}.
 * 
 * @author Herve Quiroz
 */
public final class TubularIterables
{
    private TubularIterables()
    {
        // No instantiation
    }

    /**
     * Build an {@link Iterable} sequence of element from a {@link Iterator}
     * provided by a {@link Supplier}.
     */
    public static <T> Iterable<T> newIterable(final Supplier<Iterator<T>> iteratorSupplier)
    {
        return new IteratorIterable<T>(iteratorSupplier);
    }

    private static class IteratorIterable<T> extends AbstractImmutableObject implements Iterable<T>
    {
        private final Supplier<Iterator<T>> iteratorSupplier;

        public IteratorIterable(final Supplier<Iterator<T>> iteratorSupplier)
        {
            super(iteratorSupplier);
            Preconditions.checkNotNull(iteratorSupplier);
            this.iteratorSupplier = iteratorSupplier;
        }

        @Override
        public Iterator<T> iterator()
        {
            return iteratorSupplier.get();
        }
    }

    /**
     * Get the last argument from a sequence, or <code>null</code> if there is
     * no such last element.
     */
    @ReturnsNullable
    public static <T> T getLast(final Iterable<T> elements)
    {
        return getLast(elements, null);
    }

    /**
     * Get the last argument from a sequence, or <code>defaultElement</code> if
     * there is no such last element.
     */
    @ReturnsNullable
    public static <T> T getLast(final Iterable<T> elements, final T defaultElement)
    {
        try
        {
            return Iterables.getLast(elements);
        }
        catch (final NoSuchElementException e)
        {
            return defaultElement;
        }
    }

    public static <T> Iterable<T> append(final Iterable<T> iterable, final T... elements)
    {
        return Iterables.concat(iterable, ImmutableList.of(elements));
    }

    public static <T> Iterable<T> prepend(final Iterable<T> iterable, final T... elements)
    {
        return Iterables.concat(ImmutableList.of(elements), iterable);
    }

    /**
     * Compute a sequence of results by applying each function form the list to
     * the same argument.
     */
    public static <F, T> Iterable<T> applyFunctions(final Iterable<Function<F, T>> functions, final F argument)
    {
        final Function<Function<F, T>, T> applyFunction = TranceCodeFunctions.applyTo(argument);
        return Iterables.transform(functions, applyFunction);
    }

    public static <T> Iterable<T> getDescendants(final Iterable<T> parentElements,
            final Function<T, Iterable<T>> getChildFunction)
    {
        if (Iterables.isEmpty(parentElements))
        {
            return parentElements;
        }

        final Iterable<T> children = Iterables.concat(Iterables.transform(parentElements, getChildFunction));

        return Iterables.concat(parentElements, getDescendants(children, getChildFunction));
    }

    public static <T> Iterable<T> getDescendants(final T parentElement, final Function<T, Iterable<T>> getChildFunction)
    {
        return getDescendants(ImmutableList.of(parentElement), getChildFunction);
    }

    public static boolean removeAll(final Iterable<?> iterable)
    {
        boolean removed = false;
        for (final Iterator<?> iterator = iterable.iterator(); iterator.hasNext();)
        {
            iterator.next();
            iterator.remove();
            removed = true;
        }

        return removed;
    }
}
