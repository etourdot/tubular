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
 */
public class XProcAttributes
{
    public static final QName HREF = new QName("href");
    public static final QName KIND = new QName("kind");
    public static final QName NAME = new QName("name");
    public static final QName PORT = new QName("port");
    public static final QName PRIMARY = new QName("primary");
    public static final QName REQUIRED = new QName("required");
    public static final QName SELECT = new QName("select");
    public static final QName SEQUENCE = new QName("sequence");
    public static final QName STEP = new QName("step");
    public static final QName TEST = new QName("test");
    public static final QName TYPE = new QName("type");
    public static final QName VALUE = new QName("value");
    public static final QName VERSION = new QName("version");

    private XProcAttributes()
    {
        // No instantiation
    }
}
