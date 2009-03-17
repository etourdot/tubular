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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import org.junit.BeforeClass;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public abstract class AbstractTest
{
	public static final String PROPERTY_QUIET = AbstractTest.class.getName() + ".QUIET";

	public static final boolean QUIET = Boolean.getBoolean(PROPERTY_QUIET);

	protected final XLogger log = XLoggerFactory.getXLogger(getClass());


	@BeforeClass
	public static void setupLogging()
	{
		Logger.getRootLogger().removeAllAppenders();
		if (!QUIET)
		{
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p %30.30c{2} %-30M %m%n")));
			Logger.getLogger("org.trancecode").setLevel(Level.TRACE);
		}
	}
}
