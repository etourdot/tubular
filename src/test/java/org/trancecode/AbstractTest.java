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
package org.trancecode;

import org.trancecode.log.Log4jLogger;
import org.trancecode.log.Logger;
import org.trancecode.log.LoggerManager;
import org.trancecode.log.NullLogger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import org.junit.BeforeClass;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractTest
{
	public static final String PROPERTY_QUIET = AbstractTest.class.getName() + ".QUIET";

	public static final Level TRACE = Level.TRACE;
	public static final Level DEBUG = Level.DEBUG;
	public static final Level INFO = Level.INFO;
	public static final Level WARN = Level.WARN;
	public static final Level ERROR = Level.ERROR;
	public static final Level FATAL = Level.FATAL;

	public static final boolean QUIET = Boolean.getBoolean(PROPERTY_QUIET);

	protected final Logger log = LoggerManager.getLogger(this);


	@BeforeClass
	public static void setupLogging()
	{
		if (QUIET)
		{
			LoggerManager.setRootLogger(NullLogger.INSTANCE);
		}
		else
		{
			BasicConfigurator.configure();
			LoggerManager.setRootLogger(Log4jLogger.ROOT_LOGGER);
		}
	}


	protected static void setLoggingLevel(final String loggerName, final Level level)
	{
		org.apache.log4j.Logger.getLogger(loggerName).setLevel(level);
	}


	protected static void setLoggingLevel(final Class<?> c, final Level level)
	{
		org.apache.log4j.Logger.getLogger(c).setLevel(level);
	}
}
