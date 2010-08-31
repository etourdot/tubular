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
package org.trancecode.xml;

import com.google.common.io.Closeables;
import org.xml.sax.InputSource;

/**
 * Utility methods related to SAX.
 * 
 * @author Herve Quiroz
 * @version $Revision: 3842 $
 */
public final class Sax
{
    private Sax()
    {
        // No instantiation
    }

    public static void closeQuietly(final InputSource inputSource)
    {
        if (inputSource == null)
        {
            return;
        }

        Closeables.closeQuietly(inputSource.getByteStream());
        Closeables.closeQuietly(inputSource.getCharacterStream());
    }
}
