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
package org.trancecode.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class DefaultInputResolver implements InputResolver
{
	public static final DefaultInputResolver INSTANCE = new DefaultInputResolver();


	private DefaultInputResolver()
	{
		// Only a single instance needed
	}


	@Override
	public InputStream resolveInputStream(final String href, final String base)
	{
		final URI uri = UriUtil.resolve(href, base);
		if (uri.getScheme() == null || "file".equals(uri.getScheme()))
		{
			final File file = new File(uri);
			try
			{
				return new FileInputStream(file);
			}
			catch (final IOException e)
			{
				throw new RuntimeIOException(e, "error resolving input: href = %s ; base = %s", href, base);
			}
		}

		throw new UnsupportedOperationException("URI scheme not supported: " + uri.getScheme());
	}
}
