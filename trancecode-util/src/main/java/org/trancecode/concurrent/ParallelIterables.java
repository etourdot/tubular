/*
 * Copyright (C) 2010 TranceCode Software
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
package org.trancecode.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Parallel equivalent of {@link Iterables}.
 * 
 * @author Herve Quiroz
 */
public final class ParallelIterables
{
    /**
     * @see Iterables#transform(Iterable, Function)
     */
    public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,
            final Function<? super F, ? extends T> function, final ExecutorService executor)
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return ParallelIterators.transform(fromIterable.iterator(), function, executor);
            }
        };
    }

    /**
     * @see Iterables#filter(Iterable, Predicate)
     */
    public static <T> Iterable<T> filter(final Iterable<T> unfiltered, final Predicate<? super T> predicate,
            final ExecutorService executorService)
    {
        // TODO
        return Iterables.filter(unfiltered, predicate);
    }

    public static <T> Iterable<T> get(final Iterable<Future<T>> futures)
    {
        final Function<Future<T>, T> function = FutureFunctions.get();
        return Iterables.transform(futures, function);
    }

    private ParallelIterables()
    {
        // No instantiation
    }
}
