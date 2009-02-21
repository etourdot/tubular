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
public class ConsoleLogger extends AbstractLogger
{
	private final String name;


	public ConsoleLogger()
	{
		this("");
	}


	public ConsoleLogger(final String name)
	{
		assert name != null;
		this.name = name;
	}


	@Override
	protected int getDebugLevel()
	{
		return 0;
	}


	@Override
	protected int getErrorLevel()
	{
		return 0;
	}


	@Override
	protected int getFatalLevel()
	{
		return 0;
	}


	@Override
	protected int getInfoLevel()
	{
		return 0;
	}


	@Override
	protected int getTraceLevel()
	{
		return 0;
	}


	@Override
	protected int getWarnLevel()
	{
		return 0;
	}


	@Override
	protected boolean isLevelEnabled(final int level)
	{
		return true;
	}


	@Override
	protected void logNative(final int level, final String message)
	{
		System.err.println(name + " - " + message);
	}


	@Override
	protected void logThrowable(final int level, final Throwable throwable)
	{
		System.err.println(name + " - " + throwable.getMessage());
		throwable.printStackTrace();
	}


	@Override
	public Logger getChildLogger(final String... nameElements)
	{
		final StringBuilder buffer = new StringBuilder(name);
		for (final String nameElement : nameElements)
		{
			if (buffer.length() > 0)
			{
				buffer.append(".");
			}
			buffer.append(nameElement);
		}
		return new ConsoleLogger(buffer.toString());
	}
}
