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
	QName HREF = new QName("href");
	QName KIND = new QName("kind");
	QName NAME = new QName("name");
	QName PORT = new QName("port");
	QName PRIMARY = new QName("primary");
	QName REQUIRED = new QName("required");
	QName SELECT = new QName("select");
	QName SEQUENCE = new QName("sequence");
	QName STEP = new QName("step");
	QName TEST = new QName("test");
	QName TYPE = new QName("type");
	QName VALUE = new QName("value");
}
