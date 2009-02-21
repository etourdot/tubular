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

	QName OPTION_BYTE_ORDER_MARK = new QName("byte-order-mark");

	QName OPTION_CDATA_SECTION_ELEMENTS = new QName("cdata-section-elements");

	QName OPTION_DOCTYPE_PUBLIC = new QName("doctype-public");

	QName OPTION_DOCTYPE_SYSTEM = new QName("doctype-system");

	QName OPTION_DTD_VALIDATE = new QName("dtd-validate");

	QName OPTION_ENCODING = new QName("encoding");

	QName OPTION_ESCAPE_URI_ATTRIBUTES = new QName("escape-uri-attributes");

	QName OPTION_HREF = new QName("href");

	QName OPTION_INCLUDE_CONTENT_TYPE = new QName("include-content-type");

	QName OPTION_INDENT = new QName("indent");

	QName OPTION_INITIAL_MODE = new QName("initial-mode");

	QName OPTION_LIMIT = new QName("limit");

	QName OPTION_MEDIA_TYPE = new QName("media-type");

	QName OPTION_METHOD = new QName("method");

	QName OPTION_NORMALIZATION_FORM = new QName("normalization-form");

	QName OPTION_OMIT_XML_DECLARATION = new QName("omit-xml-declaration");

	QName OPTION_OUTPUT_BASE_URI = new QName("output-base-uri");

	QName OPTION_RESULT_BASE_URI = new QName("result-base-uri");

	QName OPTION_RESULT_DOCUMENT_URI = new QName("result-document-uri");

	QName OPTION_SOURCE_BASE_URI = new QName("source-base-uri");

	QName OPTION_SOURCE_DOCUMENT_URI = new QName("source-document-uri");

	QName OPTION_STANDALONE = new QName("standalone");

	QName OPTION_TEMP_BASE_URI = new QName("temp-base-uri");

	QName OPTION_TEMPLATE_NAME = new QName("template-name");

	QName OPTION_TEST = new QName("test");

	QName OPTION_UNDECLARE_PREFIXES = new QName("undeclare-prefixes");

	QName OPTION_VALIDATE = new QName("validate");

	QName OPTION_VERSION = new QName("version");
}
