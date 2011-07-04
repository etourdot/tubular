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
package org.trancecode.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.trancecode.collection.MapFunctions;
import org.trancecode.collection.TcIterators;
import org.trancecode.function.TcPredicates;

/**
 * Parallel equivalent of {@link Iterators}.
 * 
 * @author Herve Quiroz
 */
public final class ParallelIterators
{
    private static final int MAXIMUM_NUMBER_OF_ELEMENTS_IN_ADVANCE = 1024;

    private static final Future<?> LAST = new Future<Object>()
    {
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDone()
        {
            throw new UnsupportedOperationException();
        }
    };

    private static <T> Future<T> last()
    {
        @SuppressWarnings("unchecked")
        final Future<T> last = (Future<T>) LAST;
        return last;
    }

    /**
     * @see Iterators#transform(Iterator, Function)
     */
    public static <F, T> Iterator<T> transform(final Iterator<F> fromIterable,
            final Function<? super F, ? extends T> function, final ExecutorService executor)
    {
        final Function<F, Callable<T>> applyFunction = CallableFunctions.apply(function);
        final Iterator<Callable<T>> callables = Iterators.transform(fromIterable, applyFunction);

        final AtomicReference<Future<?>> submitTask = new AtomicReference<Future<?>>();

        final BlockingQueue<Future<T>> futures = new ArrayBlockingQueue<Future<T>>(
                MAXIMUM_NUMBER_OF_ELEMENTS_IN_ADVANCE);
        final Function<Callable<T>, Future<T>> submitFunction = new Function<Callable<T>, Future<T>>()
        {
            @Override
            public Future<T> apply(final Callable<T> task)
            {
                return executor.submit(new Callable<T>()
                {
                    @Override
                    public T call() throws Exception
                    {
                        try
                        {
                            return task.call();
                        }
                        catch (final Exception e)
                        {
                            final Future<?> taskToCancel = submitTask.get();
                            if (taskToCancel != null)
                            {
                                taskToCancel.cancel(true);
                                final Future<T> last = last();
                                futures.add(last);
                            }
                            throw e;
                        }
                    }
                });
            }
        };

        submitTask.set(executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                Iterators.addAll(futures, Iterators.transform(callables, submitFunction));
                final Future<T> last = last();
                futures.add(last);
            }
        }));

        return TcIterators.handleErrors(getUntilLast(TcIterators.removeAll(futures)), new Function<Throwable, Void>()
        {
            @Override
            public Void apply(final Throwable error)
            {
                TcFutures.cancel(futures);
                return null;
            }
        });
    }

    private static <T> Iterator<T> getUntilLast(final Iterator<Future<T>> futures)
    {
        final Future<T> last = last();
        final Iterator<Future<T>> futuresUntilLast = TcIterators.until(futures, TcPredicates.identicalTo(last));
        final Function<Future<T>, T> function = FutureFunctions.get();
        return Iterators.transform(futuresUntilLast, function);
    }

    /**
     * @see Iterators#filter(Iterable, Predicate)
     */
    public static <T> Iterator<T> filter(final Iterator<T> unfiltered, final Predicate<? super T> predicate,
            final ExecutorService executor)
    {
        final Function<? super T, Entry<T, Boolean>> evaluateFunction = new Function<T, Entry<T, Boolean>>()
        {
            @Override
            public Entry<T, Boolean> apply(final T element)
            {
                return Maps.immutableEntry(element, predicate.apply(element));
            }
        };

        final Iterator<Entry<T, Boolean>> unfilteredWithPredicateEvaluated = transform(unfiltered, evaluateFunction,
                executor);
        final Function<Entry<T, Boolean>, Boolean> getValueFunction = MapFunctions.getValue();
        final Iterator<Entry<T, Boolean>> filtered = Iterators.filter(unfilteredWithPredicateEvaluated,
                TcPredicates.asPredicate(getValueFunction));

        final Function<Entry<T, Boolean>, T> getKeyFunction = MapFunctions.getKey();
        return Iterators.transform(filtered, getKeyFunction);
    }

    private ParallelIterators()
    {
        // No instantiation
    }
}
