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
package org.trancecode.log;

/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractLogger implements Logger
{
	public static final String NULL = "null";


	protected abstract int getTraceLevel();


	protected abstract int getDebugLevel();


	protected abstract int getInfoLevel();


	protected abstract int getWarnLevel();


	protected abstract int getErrorLevel();


	protected abstract int getFatalLevel();


	protected abstract boolean isLevelEnabled(int level);


	protected abstract void logNative(int level, String message);


	protected abstract void logThrowable(int level, Throwable throwable);


	private void log(final int level, final Object message, final Object... parameters)
	{
		if (isLevelEnabled(level))
		{
			if (message instanceof Throwable)
			{
				assert parameters != null;
				logThrowable(level, (Throwable)message);
			}

			final String formattedMessage = format(message, parameters);
			logNative(level, formattedMessage);
		}
	}


	private final String format(final Object message, final Object... parameters)
	{
		try
		{
			final String stringMessage = toString(message);

			if (parameters == null || parameters.length == 0)
			{
				return stringMessage;
			}

			final Object[] stringParameters = new String[parameters.length];
			for (int i = 0; i < parameters.length; i++)
			{
				stringParameters[i] = toString(parameters[i]);
			}

			return String.format(stringMessage, stringParameters);
		}
		catch (final Exception e)
		{
			LoggerManager.getMetaLogger().warn("Error while formatting log message: " + message);
			LoggerManager.getMetaLogger().trace(e);
			return null;
		}
	}


	private String toString(final Object object)
	{
		if (object == null)
		{
			return NULL;
		}

		// TODO allow custom renderers
		return object.toString();
	}


	public final void debug(final Object message, final Object... parameters)
	{
		log(getDebugLevel(), message, parameters);
	}


	public final void error(final Object message, final Object... parameters)
	{
		log(getErrorLevel(), message, parameters);
	}


	public final void fatal(final Object message, final Object... parameters)
	{
		log(getFatalLevel(), message, parameters);
	}


	public final void info(final Object message, final Object... parameters)
	{
		log(getInfoLevel(), message, parameters);
	}


	public final void trace(final Object message, final Object... parameters)
	{
		log(getTraceLevel(), message, parameters);
	}


	public final void warn(final Object message, final Object... parameters)
	{
		log(getWarnLevel(), message, parameters);
	}
}
