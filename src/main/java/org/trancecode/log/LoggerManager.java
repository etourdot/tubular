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
public final class LoggerManager
{
	protected static final String PACKAGE_LOG = AbstractLogger.class.getPackage().getName();

	private static Logger rootLogger;
	private static final Logger metaLogger = new ConsoleLogger();
	private static LoggerFactory loggerFactory = new DefaultLoggerFactory();


	private static class DefaultLoggerFactory implements LoggerFactory
	{
		public Logger getLogger(final Logger rootLogger, final Object component)
		{
			if (component instanceof Class)
			{
				final String[] nameElements = ((Class<?>)component).getName().split("\\.");
				return rootLogger.getChildLogger(nameElements);
			}

			return getLogger(rootLogger, component.getClass());
		}
	}


	private LoggerManager()
	{
		// To prevent instantiation
	}


	protected static Logger getMetaLogger()
	{
		assert metaLogger != null;
		return metaLogger;
	}


	public static Logger getLogger(final Object component)
	{
		return loggerFactory.getLogger(rootLogger, component);
	}


	public static Logger getLogger(final String... nameElements)
	{
		assert rootLogger != null;
		return rootLogger.getChildLogger(nameElements);
	}


	public static void setRootLogger(final Logger rootLogger)
	{
		assert rootLogger != null;
		LoggerManager.rootLogger = rootLogger;
	}


	public static void setLoggerFactory(final LoggerFactory loggerFactory)
	{
		LoggerManager.loggerFactory = loggerFactory;
	}
}
