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

import java.util.Map;

import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.variable.Variable;

/**
 * {@code p:in-scope-names}.
 * 
 * @author Lucas Soltic
 * @see <a
 *      href="http://www.w3.org/TR/xproc-template/#c.in-scope-names">p:in-scope-names</a>
 */
@ExternalResources(read = false, write = false)
public final class InScopeNamesStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(InScopeNamesStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.IN_SCOPE_NAMES;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        /*
         * Todo: get in scopes options; get in scopes variables; create
         * parameters with these options and variables; create a param-set with
         * these parameters; output the param-set
         */
        final Step stepInput = input.getStep();
        final Map<QName, Variable> variables = stepInput.getVariables();
        LOG.trace("available variables = {}", variables);
    }
}
