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
package org.trancecode.xproc;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;


/**
 * Utility methods related to {@link Variable}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class Variables
{
	private Variables()
	{
		// No instantiation
	}


	public static boolean containsVariable(final Iterable<Variable> variables, final QName name)
	{
		return Iterables.any(variables, VariablePredicates.isNamed(name));
	}


	public static Variable getVariable(final Iterable<Variable> variables, final QName name)
	{
		return Iterables.getOnlyElement(Iterables.filter(variables, VariablePredicates.isNamed(name)));
	}


	public static Variable getOption(final Iterable<Variable> variables, final QName name)
	{
		return Iterables.getOnlyElement(Iterables.filter(variables, Predicates.and(
			VariablePredicates.isOption(), VariablePredicates.isNamed(name))));
	}


	public static Iterable<Variable> setVariable(
		final Iterable<Variable> variables, final QName name, final Variable variable)
	{
		assert containsVariable(variables, name);

		return Iterables.transform(variables, new Function<Variable, Variable>()
		{
			@Override
			public Variable apply(final Variable currentVariable)
			{
				if (currentVariable.getName().equals(name))
				{
					return variable;
				}

				return currentVariable;
			}
		});
	}


	public static Iterable<Variable> setVariable(final Iterable<Variable> variables, final Variable variable)
	{
		assert containsVariable(variables, variable.getName());

		return Iterables.transform(variables, new Function<Variable, Variable>()
		{
			@Override
			public Variable apply(final Variable currentVariable)
			{
				if (currentVariable.getName().equals(variable.getName()))
				{
					return variable;
				}

				return currentVariable;
			}
		});
	}
}
