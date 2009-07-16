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
package org.trancecode.core.function;

import org.trancecode.core.AbstractImmutableObject;

import com.google.common.base.Function;


/**
 * Utility methods related to {@link Function}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class TubularFunctions
{
	private TubularFunctions()
	{
		// No instantiation
	}


	public static <E, P> E apply(
		final E initialElement, final Iterable<P> parameters, final Function<Pair<E, P>, E> function)
	{
		E currentElement = initialElement;
		for (final P parameter : parameters)
		{
			currentElement = function.apply(Pairs.newImmutablePair(currentElement, parameter));
		}

		return currentElement;
	}


	public static <F, T> Function<Function<F, T>, T> applyTo(final F argument)
	{
		return new ApplyFunction<F, T>(argument);
	}


	private static class ApplyFunction<F, T> extends AbstractImmutableObject implements Function<Function<F, T>, T>
	{
		private final F argument;


		public ApplyFunction(final F argument)
		{
			super(argument);
			this.argument = argument;
		}


		@Override
		public T apply(final Function<F, T> function)
		{
			return function.apply(argument);
		}
	}
}
