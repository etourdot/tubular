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

import net.sf.saxon.s9api.QName;
import org.trancecode.xproc.port.Port;
import org.trancecode.xproc.port.XProcPorts;

/**
 * @author Herve Quiroz
 */
public final class GroupStepProcessor extends AbstractCompoundStepProcessor
{
    public static final StepProcessor INSTANCE = new GroupStepProcessor();
    public static final Step STEP = Step.newStep(XProcSteps.GROUP, INSTANCE, true).declarePort(
            Port.newInputPort(XProcPorts.SOURCE).setSequence(true).setPrimary(true));

    private GroupStepProcessor()
    {
        // Singleton
    }

    @Override
    public QName stepType()
    {
        return XProcSteps.GROUP;
    }
}
