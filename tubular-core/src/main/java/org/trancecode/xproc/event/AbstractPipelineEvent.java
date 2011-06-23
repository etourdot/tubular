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

import com.google.common.base.Preconditions;
import org.trancecode.xproc.step.Step;

/**
 * @author Herve Quiroz
 */
abstract class AbstractPipelineEvent extends AbstractXProcEvent implements PipelineEvent
{
    private final Step pipeline;

    public AbstractPipelineEvent(final Step pipeline)
    {
        this.pipeline = Preconditions.checkNotNull(pipeline);
    }

    @Override
    public Step getPipeline()
    {
        return pipeline;
    }
}
