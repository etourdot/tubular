/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.trancecode.xproc.binding;

import org.trancecode.xml.Location;
import org.trancecode.xproc.EnvironmentPortBinding;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.PortBinding;

import java.util.Collections;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmNode;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class DocumentPortBinding extends AbstractPortBinding implements PortBinding
{
	private final String href;


	// TODO cache support

	public DocumentPortBinding(final String href, final Location location)
	{
		super(location);

		this.href = href;
	}


	public Iterable<XdmNode> readNodes()
	{
		throw new IllegalStateException();
	}


	@Override
	public EnvironmentPortBinding newEnvironmentPortBinding(final Environment environment)
	{
		return new AbstractBoundPortBinding(location)
		{
			public Iterable<XdmNode> readNodes()
			{
				try
				{
					final Source source =
						environment.getConfiguration().getUriResolver().resolve(href, location.getSystemId());

					return Collections.singletonList(environment.getProcessor().newDocumentBuilder().build(source));
				}
				catch (final Exception e)
				{
					throw new PipelineException("Error while trying to build document ; href = %s", e, href);
				}
			}
		};
	}


	@Override
	public String toString()
	{
		return String.format("%s[%s]", getClass().getSimpleName(), href);
	}
}
