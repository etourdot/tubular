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
import com.google.common.base.Supplier;

import java.util.Map;

import org.trancecode.api.Idempotent;

/**
 * Utility methods related to {@link Supplier}.
 * 
 * @author Herve Quiroz
 */
public final class TcSuppliers
{
    private TcSuppliers()
    {
        // No instantiation
    }

    public static <F, T> Supplier<T> fromFunction(final Function<F, T> function, final F argument)
    {
        return new FunctionSupplier<>(function, argument);
    }

    private static class FunctionSupplier<F, T> implements Supplier<T>
    {
        private final Function<F, T> function;
        private final F argument;

        public FunctionSupplier(final Function<F, T> function, final F argument)
        {
            this.function = Preconditions.checkNotNull(function);
            this.argument = Preconditions.checkNotNull(argument);
        }

        @Override
        public T get()
        {
            return function.apply(argument);
        }
    }

    public static <T> Supplier<T> singleton(final T value)
    {
        return () -> value;
    }

    public static <T> Supplier<T> memoize(final Supplier<T> supplier)
    {
        return new Supplier<T>()
        {
            private Supplier<T> value = new Supplier<T>()
            {
                @Override
                public T get()
                {
                    final T computedValue = supplier.get();
                    value = singleton(computedValue);
                    return computedValue;
                }
            };

            @Override
            @Idempotent
            public T get()
            {
                return value.get();
            }
        };
    }

    public static <T> Supplier<T> getFromMap(final Map<?, ?> map, final Object key)
    {
        Preconditions.checkNotNull(map);
        Preconditions.checkNotNull(key);
        return () -> {
            @SuppressWarnings("unchecked")
            final T value = (T) map.get(key);
            return value;
        };
    }
}
