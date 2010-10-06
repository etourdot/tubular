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
package org.trancecode.parallel;

import com.google.common.base.Predicate;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * {@link Predicate} implementations related to concurrent processing.
 * 
 * @author Herve Quiroz
 * @see Future
 * @see Callable
 */
public final class ParallelPredicates
{
    /**
     * @see Future#isCancelled()
     */
    public static <T> Predicate<Future<T>> isCancelled()
    {
        @SuppressWarnings("unchecked")
        final IsCancelledPredicate<T> predicate = (IsCancelledPredicate<T>) IsCancelledPredicate.INSTANCE;
        return predicate;
    }

    private static final class IsCancelledPredicate<T> implements Predicate<Future<T>>
    {
        private static final IsCancelledPredicate<?> INSTANCE = new IsCancelledPredicate<Object>();

        @Override
        public boolean apply(final Future<T> task)
        {
            return task.isCancelled();
        }
    }

    private ParallelPredicates()
    {
        // No instantiation
    }
}
