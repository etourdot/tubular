/*
 * Copyright (C) 2011 Emmanuel Tourdot
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
import org.trancecode.xproc.XProcExceptions;

/**
 * User: Emmanuel Tourdot
 * Date: 12 febr. 2011
 * Time: 21:41:42
 */
public final class StepUtils
{
    private StepUtils()
    {
        // No instantiation
    }

    public static QName getNewNamespace(final String newPrefix, final String newNamespace, final String newName,
            final Step step)
    {
        if (newPrefix != null)
        {
            if (newNamespace == null)
            {
                throw XProcExceptions.xd0034(step.getLocation());
            }
            else
            {
                return new QName(newPrefix, newNamespace, newName);
            }
        }
        else
        {
            if (newNamespace == null)
            {
                return new QName(newName, step.getNode());
            }
        }
        if (newName.contains(":"))
        {
            throw XProcExceptions.xd0034(step.getLocation());
        }
        else
        {
            return new QName("", newNamespace, newName);
        }
    }

}
