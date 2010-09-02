/*
 * Copyright (C) 2010 TranceCode Software
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
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;

/**
 * {@code p:sink}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.sink">p:sink</a>
 */
public final class SinkStepProcessor extends AbstractStepProcessor
{
    public static final SinkStepProcessor INSTANCE = new SinkStepProcessor();
    private static final Logger LOG = Logger.getLogger(SinkStepProcessor.class);

    @Override
    public QName stepType()
    {
        return XProcSteps.SINK;
    }

    @Override
    protected Environment doRun(final Step step, final Environment environment) throws Exception
    {
        LOG.trace("{@method} step = {}", step.getName());
        return environment;
    }
}
