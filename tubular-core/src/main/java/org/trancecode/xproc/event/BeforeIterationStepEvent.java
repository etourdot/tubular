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
package org.trancecode.xproc.event;

import org.trancecode.xproc.Environment;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public final class BeforeIterationStepEvent extends AbstractStepEvent
{
    private final int iterationPosition;
    private final int iterationSize;
    private final Environment sourceEnvironment;

    public BeforeIterationStepEvent(final Step pipeline, final Step step, final Environment sourceEnvironment,
            final int iterationPosition, final int iterationSize)
    {
        super(pipeline, step);
        this.sourceEnvironment = sourceEnvironment;
        this.iterationPosition = iterationPosition;
        this.iterationSize = iterationSize;
    }

    public Environment getSourceEnvironment()
    {
        return sourceEnvironment;
    }

    public int getIterationPosition()
    {
        return iterationPosition;
    }

    public int getIterationSize()
    {
        return iterationSize;
    }
}
