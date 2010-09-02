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
package org.trancecode.xproc.step;

import com.google.common.base.Function;
import org.trancecode.function.Pair;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public final class StepFunctions
{
    private StepFunctions()
    {
        // No instantiation
    }

    public static Function<Pair<Step, Variable>, Step> declareVariable()
    {
        return DeclareVariableBinaryFunction.INSTANCE;
    }

    private static class DeclareVariableBinaryFunction implements Function<Pair<Step, Variable>, Step>
    {
        private static final DeclareVariableBinaryFunction INSTANCE = new DeclareVariableBinaryFunction();

        @Override
        public Step apply(final Pair<Step, Variable> arguments)
        {
            final Step step = arguments.left();
            final Variable variable = arguments.right();
            return step.declareVariable(variable);
        }
    }
}
