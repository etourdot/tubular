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
import org.trancecode.xproc.Pipeline;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
public final class AfterExecuteStepEvent extends AbstractStepEvent
{
    private final Environment resultEnvironment;
    private final Environment sourceEnvironment;

    public AfterExecuteStepEvent(final Pipeline pipeline, final Step step, final Environment sourceEnvironment,
            final Environment resultEnvironment)
    {
        super(pipeline, step);
        this.sourceEnvironment = sourceEnvironment;
        this.resultEnvironment = resultEnvironment;
    }

    public Environment getSourceEnvironment()
    {
        return sourceEnvironment;
    }

    public Environment getResultEnvironment()
    {
        return resultEnvironment;
    }
}
