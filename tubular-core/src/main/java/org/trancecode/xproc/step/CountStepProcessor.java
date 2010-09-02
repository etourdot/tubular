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

import com.google.common.collect.Iterables;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:count}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.count">p:count</a>
 */
public class CountStepProcessor extends AbstractStepProcessor
{
    public static final CountStepProcessor INSTANCE = new CountStepProcessor();

    private static final Logger LOG = Logger.getLogger(CountStepProcessor.class);

    @Override
    protected Environment doRun(final Step step, final Environment environment)
    {
        LOG.trace("{@method} step = {}", step.getName());
        assert step.getType().equals(XProcSteps.COUNT);

        // TODO improve performance with "limit" option
        final int count = Iterables.size(environment.readNodes(step.getPortReference(XProcPorts.SOURCE)));
        LOG.trace("  count = {}", count);
        final int limit = Integer.parseInt(environment.getVariable(XProcOptions.LIMIT));
        LOG.trace("  limit = {}", limit);
        final int result = (limit > 0 ? Math.min(count, limit) : count);
        LOG.trace("  result = {}", result);

        return environment.writeNodes(step.getPortReference(XProcPorts.RESULT),
                environment.newResultElement(Integer.toString(result)));
    }
}
