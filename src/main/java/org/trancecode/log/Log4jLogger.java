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

import org.apache.log4j.Level;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class Log4jLogger extends AbstractLogger
{
	public static final String NAME_DELIMITER = ".";

	public static final Logger ROOT_LOGGER = new Log4jLogger(org.apache.log4j.Logger.getRootLogger());

	private final org.apache.log4j.Logger logger;


	private Log4jLogger(final org.apache.log4j.Logger logger)
	{
		this.logger = logger;
	}


	@Override
	protected int getDebugLevel()
	{
		return Level.DEBUG_INT;
	}


	@Override
	protected int getErrorLevel()
	{
		return Level.ERROR_INT;
	}


	@Override
	protected int getFatalLevel()
	{
		return Level.FATAL_INT;
	}


	@Override
	protected int getInfoLevel()
	{
		return Level.INFO_INT;
	}


	@Override
	protected int getTraceLevel()
	{
		return Level.TRACE_INT;
	}


	@Override
	protected int getWarnLevel()
	{
		return Level.WARN_INT;
	}


	public Logger getChildLogger(final String... nameElements)
	{
		final StringBuilder name = new StringBuilder();
		if (logger != org.apache.log4j.Logger.getRootLogger())
		{
			name.append(logger.getName());
		}

		for (final String nameElement : nameElements)
		{
			assert nameElement != null;
			if (name.length() > 0)
			{
				name.append(NAME_DELIMITER);
			}

			name.append(nameElement);
		}

		final String normalizedName = name.toString().replaceAll("\\.\\+", ".");
		return new Log4jLogger(org.apache.log4j.Logger.getLogger(normalizedName));
	}


	@Override
	protected boolean isLevelEnabled(final int level)
	{
		return logger.isEnabledFor(Level.toLevel(level));
	}


	@Override
	protected void logNative(final int level, final String message)
	{
		logger.log(Level.toLevel(level), message);
	}


	@Override
	protected void logThrowable(final int level, final Throwable throwable)
	{
		logger.log(Level.toLevel(level), throwable, throwable);
	}
}
