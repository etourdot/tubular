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
package org.trancecode.xproc;

import org.trancecode.core.InterningPool;

/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class NamePool
{
    private final InterningPool<String> strings = InterningPool.newInstance();
    private final InterningPool<PortName> portNames = InterningPool.newInstance();
    private final InterningPool<StepName> stepNames = InterningPool.newInstance();
    private final InterningPool<PortReference> portReferences = InterningPool.newInstance();

    public String newString(final String string)
    {
        return strings.intern(string);
    }

    public PortName newPortName(final String name)
    {
        return portNames.intern(PortName.newInstance(name));
    }

    public StepName newStepName(final String name)
    {
        return stepNames.intern(StepName.newInstance(name));
    }

    public PortReference newPortReference(final StepName stepName, final PortName portName)
    {
        return portReferences.intern(PortReference.newReference(stepName.toString(), portName.toString()));
    }
}
