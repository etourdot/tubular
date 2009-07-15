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
package org.trancecode.core.collection;

import org.trancecode.annotation.ReturnsNullable;
import org.trancecode.core.AbstractImmutableObject;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;


/**
 * Utility methods related to {@link Iterable}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class TubularIterables
{
	private TubularIterables()
	{
		// No instantiation
	}


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


	@ReturnsNullable
	public static <T> T getLast(final Iterable<T> elements)
	{
		if (elements instanceof List)
		{
			return getLast(elements);
		}
	
		try
		{
			return Iterators.getLast(elements.iterator());
		}
		catch (final NoSuchElementException e)
		{
			return null;
		}
	}


	@ReturnsNullable
	public static <T> T getLast(final List<T> elements)
	{
		if (elements.isEmpty())
		{
			return null;
		}
	
		return elements.get(elements.size() - 1);
	}


	public static <T> Iterable<T> append(final Iterable<T> iterable, final T... elements)
	{
		return Iterables.concat(iterable, ImmutableList.of(elements));
	}
}
