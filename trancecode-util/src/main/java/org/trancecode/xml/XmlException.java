/*
 * Copyright (C) 2010 Herve Quiroz
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
package org.trancecode.xml;

import org.trancecode.base.BaseException;

/**
 * XML-related exception.
 * 
 * @author Herve Quiroz
 */
public class XmlException extends BaseException
{
    private static final long serialVersionUID = 3358006625441527847L;

    public XmlException(final Throwable cause, final String message, final Object... parameters)
    {
        super(cause, message, parameters);
    }
}
