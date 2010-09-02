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
package org.trancecode.xproc.variable;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import net.sf.saxon.s9api.QName;
import org.trancecode.collection.TcIterables;
import org.trancecode.function.TcFunctions;

/**
 * Utility methods related to {@link Variable}.
 * 
 * @author Herve Quiroz
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
        return Iterables.getOnlyElement(Iterables.filter(variables, Predicates.and(VariablePredicates.isOption(),
                VariablePredicates.isNamed(name))));
    }

    public static Iterable<Variable> setVariable(final Iterable<Variable> variables, final QName name,
            final Variable variable)
    {
        assert containsVariable(variables, name);

        return Iterables.transform(variables, TcFunctions.conditional(Predicates.compose(Predicates
                .equalTo(name), VariableFunctions.getName()), Functions.constant(variable), VariableFunctions
                .identity()));
    }

    public static Iterable<Variable> setVariable(final Iterable<Variable> variables, final Variable variable)
    {
        assert containsVariable(variables, variable.getName());

        return setVariable(variables, variable.getName(), variable);
    }

    public static Iterable<Variable> setOrAddVariable(final Iterable<Variable> variables, final Variable variable)
    {
        if (containsVariable(variables, variable.getName()))
        {
            setVariable(variables, variable);
        }

        return TcIterables.append(variables, variable);
    }

    public static Iterable<QName> getVariableNames(final Iterable<Variable> variables)
    {
        return Iterables.transform(variables, VariableFunctions.getName());
    }
}
