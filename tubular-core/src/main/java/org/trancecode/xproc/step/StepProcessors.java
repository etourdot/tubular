/*
 * Copyright (C) 2011 Herve Quiroz
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
package org.trancecode.xproc.step;

import net.sf.saxon.s9api.QName;
import org.trancecode.xproc.Environment;

/**
 * Utility methods related to {@link StepProcessor}.
 * 
 * @author Herve Quiroz
 */
public final class StepProcessors
{
    public static StepProcessor unsupportedStepProcessor(final QName stepType)
    {
        return new StepProcessor()
        {
            @Override
            public QName getStepType()
            {
                return stepType;
            }

            @Override
            public Environment run(final Step step, final Environment environment)
            {
                throw new UnsupportedOperationException("step not supported: " + stepType);
            }
        };
    }

    private StepProcessors()
    {
        // No instantiation
    }
}
