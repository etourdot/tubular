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
	QName CHOOSE = NAMESPACE_XPROC.newSaxonQName("choose");

	QName COUNT = NAMESPACE_XPROC.newSaxonQName("count");

	QName FOR_EACH = NAMESPACE_XPROC.newSaxonQName("for-each");

	QName GROUP = NAMESPACE_XPROC.newSaxonQName("group");

	QName IDENTITY = NAMESPACE_XPROC.newSaxonQName("identity");

	QName LOAD = NAMESPACE_XPROC.newSaxonQName("load");

	QName OTHERWISE = NAMESPACE_XPROC.newSaxonQName("otherwise");

	QName PIPELINE = NAMESPACE_XPROC.newSaxonQName("pipeline");

	QName STORE = NAMESPACE_XPROC.newSaxonQName("store");

	QName TRY = NAMESPACE_XPROC.newSaxonQName("try");

	QName WHEN = NAMESPACE_XPROC.newSaxonQName("when");

	QName XSL_FORMATTER = NAMESPACE_XPROC.newSaxonQName("xsl-formatter");

	QName XSLT = NAMESPACE_XPROC.newSaxonQName("xslt");
}
