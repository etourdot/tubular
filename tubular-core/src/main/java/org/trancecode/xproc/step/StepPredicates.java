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
package org.trancecode.xproc.step;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * {@link Predicate} implementations related to {@link Step}.
 * 
 * @author Herve Quiroz
 */
public final class StepPredicates
{
    public static Predicate<Step> hasName(final String name)
    {
        return Predicates.compose(Predicates.equalTo(name), StepFunctions.getName());
    }

    private StepPredicates()
    {
        // No instantiation
    }
}
