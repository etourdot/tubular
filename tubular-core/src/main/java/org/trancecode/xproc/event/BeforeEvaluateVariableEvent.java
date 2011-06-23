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

import java.util.Map;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.xproc.step.Step;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public final class BeforeEvaluateVariableEvent extends AbstractVariableEvent
{
    private final XdmNode xpathContextNode;
    private final Map<QName, String> inScopeVariables;

    public BeforeEvaluateVariableEvent(final Step pipeline, final Step step, final Variable variable,
            final XdmNode xpathContextNode, final Map<QName, String> inScopeVariables)
    {
        super(pipeline, step, variable);
        this.xpathContextNode = xpathContextNode;
        this.inScopeVariables = Preconditions.checkNotNull(inScopeVariables);
    }

    public XdmNode getXpathContextNode()
    {
        return xpathContextNode;
    }
}
