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
package org.trancecode.function;

/**
 * Utility methods related to {@link Pair}.
 * 
 * @author Herve Quiroz
 */
public final class Pairs
{
    private Pairs()
    {
        // No instantiation
    }

    public static <L, R> Pair<L, R> newImmutablePair(final L left, final R right)
    {
        return new ImmutablePair<L, R>(left, right);
    }

    private static class ImmutablePair<L, R> implements Pair<L, R>
    {
        private final L left;
        private final R right;

        public ImmutablePair(final L left, final R right)
        {
            this.left = left;
            this.right = right;
        }

        @Override
        public L left()
        {
            return left;
        }

        @Override
        public R right()
        {
            return right;
        }

        // TODO equals()
        // TODO hashCode()
    }
}
