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
import com.google.common.base.Functions;

import net.sf.saxon.s9api.QName;

/**
 * {@link Function} implementations related to {@link Variable}.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class VariableFunctions
{
    private VariableFunctions()
    {
        // No instantiation
    }

    public static Function<Variable, QName> getName()
    {
        return GetNameFunction.INSTANCE;
    }

    private static class GetNameFunction implements Function<Variable, QName>
    {
        public static final GetNameFunction INSTANCE = new GetNameFunction();

        public GetNameFunction()
        {
            // Singleton
        }

        @Override
        public QName apply(final Variable variable)
        {
            return variable.getName();
        }
    }

    public static Function<Variable, Variable> identity()
    {
        return Functions.identity();
    }
}
