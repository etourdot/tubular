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
	QName CHOOSE = NAMESPACE_XPROC.newSaxonQName("choose");
	QName FOR_EACH = NAMESPACE_XPROC.newSaxonQName("for-each");
	QName GROUP = NAMESPACE_XPROC.newSaxonQName("group");
	QName OTHERWISE = NAMESPACE_XPROC.newSaxonQName("otherwise");
	QName PIPELINE = NAMESPACE_XPROC.newSaxonQName("pipeline");
	QName TRY = NAMESPACE_XPROC.newSaxonQName("try");
	QName WHEN = NAMESPACE_XPROC.newSaxonQName("when");

	// Required steps
	QName ADD_ATTRIBUTE = NAMESPACE_XPROC.newSaxonQName("add-attribute");
	QName ADD_XML_BASE = NAMESPACE_XPROC.newSaxonQName("add-xml-base");
	QName COMPARE = NAMESPACE_XPROC.newSaxonQName("compare");
	QName COUNT = NAMESPACE_XPROC.newSaxonQName("count");
	QName DELETE = NAMESPACE_XPROC.newSaxonQName("delete");
	QName DIRECTORY_LIST = NAMESPACE_XPROC.newSaxonQName("directory-list");
	QName ERROR = NAMESPACE_XPROC.newSaxonQName("error");
	QName ESCAPE_MARKUP = NAMESPACE_XPROC.newSaxonQName("escape-markup");
	QName FILTER = NAMESPACE_XPROC.newSaxonQName("filter");
	QName HTTP_REQUEST = NAMESPACE_XPROC.newSaxonQName("http-request");
	QName IDENTITY = NAMESPACE_XPROC.newSaxonQName("identity");
	QName INSERT = NAMESPACE_XPROC.newSaxonQName("insert");
	QName LABEL_ELEMENT = NAMESPACE_XPROC.newSaxonQName("label-element");
	QName LOAD = NAMESPACE_XPROC.newSaxonQName("load");
	QName MAKE_ABSOLUTE_URIS = NAMESPACE_XPROC.newSaxonQName("make-absolute-uris");
	QName NAMESPACE_RENAME = NAMESPACE_XPROC.newSaxonQName("namespace-rename");
	QName PACK = NAMESPACE_XPROC.newSaxonQName("pack");
	QName PARAMETERS = NAMESPACE_XPROC.newSaxonQName("parameters");
	QName RENAME = NAMESPACE_XPROC.newSaxonQName("rename");
	QName REPLACE = NAMESPACE_XPROC.newSaxonQName("replace");
	QName SET_ATTRIBUTES = NAMESPACE_XPROC.newSaxonQName("set-attributes");
	QName SINK = NAMESPACE_XPROC.newSaxonQName("sink");
	QName SPLIT_SEQUENCE = NAMESPACE_XPROC.newSaxonQName("split-sequence");
	QName STORE = NAMESPACE_XPROC.newSaxonQName("store");
	QName STRING_REPLACE = NAMESPACE_XPROC.newSaxonQName("string-replace");
	QName UNESCAPE_MARKUP = NAMESPACE_XPROC.newSaxonQName("unescape-markup");
	QName UNWRAP = NAMESPACE_XPROC.newSaxonQName("unwrap");
	QName WRAP = NAMESPACE_XPROC.newSaxonQName("wrap");
	QName XINXLUDE = NAMESPACE_XPROC.newSaxonQName("xinclude");
	QName XSLT = NAMESPACE_XPROC.newSaxonQName("xslt");

	// Optional steps
	QName EXEC = NAMESPACE_XPROC.newSaxonQName("exec");
	QName HASH = NAMESPACE_XPROC.newSaxonQName("hash");
	QName UUID = NAMESPACE_XPROC.newSaxonQName("uuid");
	QName VALIDATE_WITH_RELANXNG = NAMESPACE_XPROC.newSaxonQName("validate-with-relaxng");
	QName VALIDATE_WITH_SCHEMATRON = NAMESPACE_XPROC.newSaxonQName("validate-with-schematron");
	QName VALIDATE_WITH_SCHEMA = NAMESPACE_XPROC.newSaxonQName("validate-with-xml-schema");
	QName WWW_FORM_URL_DECODE = NAMESPACE_XPROC.newSaxonQName("www-form-url-decode");
	QName WWW_FORM_URL_ENCODE = NAMESPACE_XPROC.newSaxonQName("www-form-url-encode");
	QName XQUERY = NAMESPACE_XPROC.newSaxonQName("xquery");
	QName XSL_FORMATTER = NAMESPACE_XPROC.newSaxonQName("xsl-formatter");
}
