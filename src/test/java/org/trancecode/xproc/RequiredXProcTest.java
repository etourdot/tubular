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
public class RequiredXProcTest extends AbstractXProcTest implements XProcTestSuiteXmlModel
{
	@Test
	public void choose001() throws Exception
	{
		test("required/choose-001.xml");
	}


	@Test
	public void choose002() throws Exception
	{
		test("required/choose-002.xml");
	}


	@Test
	public void choose003() throws Exception
	{
		test("required/choose-003.xml");
	}


	@Test
	public void count001() throws Exception
	{
		test("required/count-001.xml");
	}


	@Test
	public void count002() throws Exception
	{
		test("required/count-002.xml");
	}


	@Test
	public void declareStep001() throws Exception
	{
		test("required/declare-step-001.xml");
	}


	@Test
	public void declareStep002() throws Exception
	{
		test("required/declare-step-002.xml");
	}


	@Test
	public void declareStep003() throws Exception
	{
		test("required/declare-step-003.xml");
	}


	@Test
	public void declareStep004() throws Exception
	{
		test("required/declare-step-004.xml");
	}


	@Test
	public void declareStep005() throws Exception
	{
		test("required/declare-step-005.xml");
	}


	@Test
	public void declareStep006() throws Exception
	{
		test("required/declare-step-006.xml");
	}


	@Test
	public void declareStep007() throws Exception
	{
		test("required/declare-step-007.xml");
	}


	@Test
	public void forEach001() throws Exception
	{
		test("required/for-each-001.xml");
	}


	@Test
	public void forEach002() throws Exception
	{
		test("required/for-each-002.xml");
	}


	@Test
	public void forEach003() throws Exception
	{
		test("required/for-each-003.xml");
	}


	@Test
	public void forEach004() throws Exception
	{
		test("required/for-each-004.xml");
	}


	@Test
	public void identity001() throws Exception
	{
		test("required/identity-001.xml");
	}


	@Test
	public void identity002() throws Exception
	{
		test("required/identity-002.xml");
	}


	@Test
	public void identity003() throws Exception
	{
		test("required/identity-003.xml");
	}


	@Test
	public void identity004() throws Exception
	{
		test("required/identity-004.xml");
	}


	@Test
	public void identity005() throws Exception
	{
		test("required/identity-005.xml");
	}


	@Test
	public void import001() throws Exception
	{
		test("required/import-001.xml");
	}


	@Test
	public void import002() throws Exception
	{
		test("required/import-002.xml");
	}


	@Test
	public void import003() throws Exception
	{
		test("required/import-003.xml");
	}


	@Test
	public void input001() throws Exception
	{
		test("required/input-001.xml");
	}


	@Test
	public void input002() throws Exception
	{
		test("required/input-002.xml");
	}


	@Test
	public void input003() throws Exception
	{
		test("required/input-003.xml");
	}


	@Test
	public void input004() throws Exception
	{
		test("required/input-004.xml");
	}


	@Test
	public void input005() throws Exception
	{
		test("required/input-005.xml");
	}


	@Test
	public void load001() throws Exception
	{
		test("required/load-001.xml");
	}


	@Test
	public void option001() throws Exception
	{
		test("required/option-001.xml");
	}


	@Test
	public void option002() throws Exception
	{
		test("required/option-002.xml");
	}


	@Test
	public void option004() throws Exception
	{
		test("required/option-004.xml");
	}


	@Test
	public void param001() throws Exception
	{
		test("required/param-001.xml");
	}


	@Test
	public void parameter001() throws Exception
	{
		test("required/parameter-001.xml");
	}


	@Test
	public void parameter002() throws Exception
	{
		test("required/parameter-002.xml");
	}


	@Test
	public void store001() throws Exception
	{
		test("required/store-001.xml");
	}


	@Test
	public void variable001() throws Exception
	{
		test("required/variable-001.xml");
	}


	@Test
	public void variable002() throws Exception
	{
		test("required/variable-002.xml");
	}


	@Test
	public void variable003() throws Exception
	{
		test("required/variable-003.xml");
	}


	@Test
	public void variable004() throws Exception
	{
		test("required/variable-004.xml");
	}


	@Test
	public void xslt001() throws Exception
	{
		test("required/xslt-001.xml");
	}


	@Test
	public void xslt002() throws Exception
	{
		test("required/xslt-002.xml");
	}


	@Test
	public void xslt003() throws Exception
	{
		test("required/xslt-003.xml");
	}


	@Test
	public void xslt004() throws Exception
	{
		test("required/xslt-004.xml");
	}


	@Test
	public void xslt005() throws Exception
	{
		test("required/xslt-005.xml");
	}
}
