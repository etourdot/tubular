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

import org.trancecode.base.BaseException;
import org.trancecode.xml.Location;

/**
 * Base {@link Exception} for errors related to pipelines.
 * 
 * @author Herve Quiroz
 */
public class PipelineException extends BaseException
{
    private static final long serialVersionUID = 1891736137590567766L;

    private final Location location;

    private static String buildVerboseMessage(final String message, final Throwable cause, final Object... args)
    {
        final StringBuilder buffer = new StringBuilder(format(message, args));

        if (cause != null)

        {
            buffer.append("\n").append("Caused by: ");
            if (cause.getMessage() != null)
            {
                buffer.append(cause.getMessage());
            }
            else
            {
                buffer.append(cause);
            }
        }

        return buffer.toString();
    }

    public PipelineException(final String message, final Object... args)
    {
        super(message, args);

        location = null;
    }

    public PipelineException(final Location location, final String message, final Object... args)
    {
        super(message, args);

        this.location = location;
    }

    public PipelineException(final Throwable cause)
    {
        super(cause);

        location = null;
    }

    public PipelineException(final Throwable cause, final String message, final Object... args)
    {
        super(buildVerboseMessage(message, cause, args), cause);

        location = null;
    }

    public PipelineException(final Throwable cause, final Location location, final String message, final Object... args)
    {
        super(buildVerboseMessage(message, cause, args), cause);

        this.location = location;
    }

    public String getMessageAndLocation()
    {
        final StringBuilder buffer = new StringBuilder();

        if (location != null)
        {
            buffer.append("At: ").append(location).append("\n");
        }

        buffer.append(getMessage());

        return buffer.toString();
    }

    public Location getLocation()
    {
        return location;
    }
}
