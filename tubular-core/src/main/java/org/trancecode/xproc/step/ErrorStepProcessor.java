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

import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.XProcException;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:error}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.error">p:error</a>
 */
public final class ErrorStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ErrorStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.ERROR;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final String code = input.getOptionValue(XProcOptions.CODE);
        final String prefix = input.getOptionValue(XProcOptions.CODE_PREFIX, null);
        final String namespace = input.getOptionValue(XProcOptions.CODE_NAMESPACE, null);

        final QName errorCode;
        if (code.contains(":"))
        {
            if (prefix != null || namespace != null)
            {
                throw XProcExceptions.xd0034(input.getStep().getLocation());
            }
            errorCode = new QName(code, input.getStep().getNode());
        }
        else if (prefix == null && namespace == null)
        {
            errorCode = new QName(code);
        }
        else
        {
            errorCode = new QName(prefix, namespace, code);
        }

        // TODO write some element to the output port

        throw new XProcException(errorCode, XProcException.Type.DYNAMIC, input.getStep().getLocation(), input.getStep()
                .getName());
    }
}
