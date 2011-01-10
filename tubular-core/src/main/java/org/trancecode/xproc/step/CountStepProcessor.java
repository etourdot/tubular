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

import com.google.common.collect.Iterables;
import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:count}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.count">p:count</a>
 */
public final class CountStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(CountStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.COUNT;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        // TODO improve performance with "limit" option
        final int count = Iterables.size(input.readNodes(XProcPorts.SOURCE));
        LOG.trace("  count = {}", count);
        final int limit = Integer.parseInt(input.getOptionValue(XProcOptions.LIMIT));
        LOG.trace("  limit = {}", limit);
        final int result = (limit > 0 ? Math.min(count, limit) : count);
        LOG.trace("  result = {}", result);

        output.writeNodes(XProcPorts.RESULT, input.newResultElement(Integer.toString(result)));
    }
}
