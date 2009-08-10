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
package org.trancecode.xproc.parser;

import net.sf.saxon.s9api.QName;


/**
 * Attributes from the XProc XML model.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcAttributes
{
	QName ATTRIBUTE_HREF = new QName("href");
	QName ATTRIBUTE_KIND = new QName("kind");
	QName ATTRIBUTE_NAME = new QName("name");
	QName ATTRIBUTE_PORT = new QName("port");
	QName ATTRIBUTE_PRIMARY = new QName("primary");
	QName ATTRIBUTE_REQUIRED = new QName("required");
	QName ATTRIBUTE_SELECT = new QName("select");
	QName ATTRIBUTE_SEQUENCE = new QName("sequence");
	QName ATTRIBUTE_STEP = new QName("step");
	QName ATTRIBUTE_TEST = new QName("test");
	QName ATTRIBUTE_TYPE = new QName("type");
	QName ATTRIBUTE_VALUE = new QName("value");
}
