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

import java.util.Collections;
import java.util.Map;

import net.sf.saxon.s9api.QName;


/**
 * Common XProc options.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcOptions
{
	Map<QName, Object> EMPTY_OPTIONS_MAP = Collections.emptyMap();
	QName BYTE_ORDER_MARK = new QName("byte-order-mark");
	QName CDATA_SECTION_ELEMENTS = new QName("cdata-section-elements");
	QName CONTENT_TYPE = new QName("content-type");
	QName DOCTYPE_PUBLIC = new QName("doctype-public");
	QName DOCTYPE_SYSTEM = new QName("doctype-system");
	QName DTD_VALIDATE = new QName("dtd-validate");
	QName ENCODING = new QName("encoding");
	QName ESCAPE_URI_ATTRIBUTES = new QName("escape-uri-attributes");
	QName HREF = new QName("href");
	QName INCLUDE_CONTENT_TYPE = new QName("include-content-type");
	QName INDENT = new QName("indent");
	QName INITIAL_MODE = new QName("initial-mode");
	QName LIMIT = new QName("limit");
	QName MEDIA_TYPE = new QName("media-type");
	QName METHOD = new QName("method");
	QName NORMALIZATION_FORM = new QName("normalization-form");
	QName OMIT_XML_DECLARATION = new QName("omit-xml-declaration");
	QName OUTPUT_BASE_URI = new QName("output-base-uri");
	QName RESULT_BASE_URI = new QName("result-base-uri");
	QName RESULT_DOCUMENT_URI = new QName("result-document-uri");
	QName SOURCE_BASE_URI = new QName("source-base-uri");
	QName SOURCE_DOCUMENT_URI = new QName("source-document-uri");
	QName STANDALONE = new QName("standalone");
	QName TEMP_BASE_URI = new QName("temp-base-uri");
	QName TEMPLATE_NAME = new QName("template-name");
	QName TEST = new QName("test");
	QName UNDECLARE_PREFIXES = new QName("undeclare-prefixes");
	QName VALIDATE = new QName("validate");
	QName VERSION = new QName("version");
}
