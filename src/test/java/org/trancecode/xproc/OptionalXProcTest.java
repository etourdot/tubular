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

import org.junit.Test;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class OptionalXProcTest extends AbstractXProcTest implements XProcTestSuiteXmlModel
{
	@Test
	public void xslt2001() throws Exception
	{
		test("optional/xslt2-001.xml");
	}


	@Test
	public void xslt2002() throws Exception
	{
		test("optional/xslt2-002.xml");
	}


	@Test
	public void xslt2003() throws Exception
	{
		test("optional/xslt2-003.xml");
	}
}
