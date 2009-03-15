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

import net.sf.saxon.s9api.QName;


/**
 * Standard XProc steps.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcSteps extends XProcNamespaces
{
	// Core steps
	QName CHOOSE = XPROC.newSaxonQName("choose");
	QName FOR_EACH = XPROC.newSaxonQName("for-each");
	QName GROUP = XPROC.newSaxonQName("group");
	QName OTHERWISE = XPROC.newSaxonQName("otherwise");
	QName PIPELINE = XPROC.newSaxonQName("pipeline");
	QName TRY = XPROC.newSaxonQName("try");
	QName WHEN = XPROC.newSaxonQName("when");

	// Required steps
	QName ADD_ATTRIBUTE = XPROC.newSaxonQName("add-attribute");
	QName ADD_XML_BASE = XPROC.newSaxonQName("add-xml-base");
	QName COMPARE = XPROC.newSaxonQName("compare");
	QName COUNT = XPROC.newSaxonQName("count");
	QName DELETE = XPROC.newSaxonQName("delete");
	QName DIRECTORY_LIST = XPROC.newSaxonQName("directory-list");
	QName ERROR = XPROC.newSaxonQName("error");
	QName ESCAPE_MARKUP = XPROC.newSaxonQName("escape-markup");
	QName FILTER = XPROC.newSaxonQName("filter");
	QName HTTP_REQUEST = XPROC.newSaxonQName("http-request");
	QName IDENTITY = XPROC.newSaxonQName("identity");
	QName INSERT = XPROC.newSaxonQName("insert");
	QName LABEL_ELEMENT = XPROC.newSaxonQName("label-element");
	QName LOAD = XPROC.newSaxonQName("load");
	QName MAKE_ABSOLUTE_URIS = XPROC.newSaxonQName("make-absolute-uris");
	QName NAMESPACE_RENAME = XPROC.newSaxonQName("namespace-rename");
	QName PACK = XPROC.newSaxonQName("pack");
	QName PARAMETERS = XPROC.newSaxonQName("parameters");
	QName RENAME = XPROC.newSaxonQName("rename");
	QName REPLACE = XPROC.newSaxonQName("replace");
	QName SET_ATTRIBUTES = XPROC.newSaxonQName("set-attributes");
	QName SINK = XPROC.newSaxonQName("sink");
	QName SPLIT_SEQUENCE = XPROC.newSaxonQName("split-sequence");
	QName STORE = XPROC.newSaxonQName("store");
	QName STRING_REPLACE = XPROC.newSaxonQName("string-replace");
	QName UNESCAPE_MARKUP = XPROC.newSaxonQName("unescape-markup");
	QName UNWRAP = XPROC.newSaxonQName("unwrap");
	QName WRAP = XPROC.newSaxonQName("wrap");
	QName XINXLUDE = XPROC.newSaxonQName("xinclude");
	QName XSLT = XPROC.newSaxonQName("xslt");

	// Optional steps
	QName EXEC = XPROC.newSaxonQName("exec");
	QName HASH = XPROC.newSaxonQName("hash");
	QName UUID = XPROC.newSaxonQName("uuid");
	QName VALIDATE_WITH_RELANXNG = XPROC.newSaxonQName("validate-with-relaxng");
	QName VALIDATE_WITH_SCHEMATRON = XPROC.newSaxonQName("validate-with-schematron");
	QName VALIDATE_WITH_SCHEMA = XPROC.newSaxonQName("validate-with-xml-schema");
	QName WWW_FORM_URL_DECODE = XPROC.newSaxonQName("www-form-url-decode");
	QName WWW_FORM_URL_ENCODE = XPROC.newSaxonQName("www-form-url-encode");
	QName XQUERY = XPROC.newSaxonQName("xquery");
	QName XSL_FORMATTER = XPROC.newSaxonQName("xsl-formatter");
}
