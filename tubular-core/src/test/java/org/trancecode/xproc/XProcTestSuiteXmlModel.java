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

import org.trancecode.xml.Namespace;

import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcTestSuiteXmlModel
{
	String NAMESPACE_XPROC_TEST_SUITE_PREFIX = "t";
	String NAMESPACE_XPROC_TEST_SUITE_URI = "http://xproc.org/ns/testsuite";
	Namespace NAMESPACE_XPROC_TEST_SUITE =
		new Namespace(NAMESPACE_XPROC_TEST_SUITE_URI, NAMESPACE_XPROC_TEST_SUITE_PREFIX);

	QName ELEMENT_TEST_SUITE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("test-suite");
	QName ELEMENT_TEST = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("test");
	QName ELEMENT_DOCUMENT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("document");
	QName ELEMENT_INPUT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("input");
	QName ELEMENT_PARAMETER = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("parameter");
	QName ELEMENT_OPTION = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("option");
	QName ELEMENT_OUTPUT = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("output");
	QName ELEMENT_PIPELINE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("pipeline");
	QName ELEMENT_COMPARE_PIPELINE = NAMESPACE_XPROC_TEST_SUITE.newSaxonQName("compare-pipeline");

	QName ATTRIBUTE_ERROR = new QName("error");
	QName ATTRIBUTE_HREF = new QName("href");
	QName ATTRIBUTE_PORT = new QName("port");
	QName ATTRIBUTE_NAME = new QName("name");
	QName ATTRIBUTE_VALUE = new QName("value");
}
