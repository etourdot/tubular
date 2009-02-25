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

import org.trancecode.xml.Location;
import org.trancecode.xproc.XProcException.Type;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public final class XProcExceptions
{
	private XProcExceptions()
	{
		// No instantiation
	}


	public static XProcException xd0004(final Location location, final Object... args)
	{
		return newXProcException(
			Type.DYNAMIC, 4, location, "no subpipeline is selected by the p:choose and no default is provided", args);
	}


	public static XProcException xs0038(final Location location, final Object... args)
	{
		return newXProcException(Type.STEP, 38, location, "XSLT version %s not supported", args);
	}


	private static XProcException newXProcException(
		final Type type, final int code, final Location location, final String message, final Object... parameters)
	{
		final XProcException exception = new XProcException(type, code, location, message, parameters);
		final StackTraceElement[] stackTrace = new StackTraceElement[exception.getStackTrace().length - 1];
		System.arraycopy(exception.getStackTrace(), 1, stackTrace, 0, stackTrace.length);
		exception.setStackTrace(stackTrace);
		return exception;
	}
}
