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

import net.sf.saxon.s9api.QName;


/**
 * Standard XProc steps.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcSteps extends XProcNamespaces
{
	QName STEP_CHOOSE = NAMESPACE_XPROC.newSaxonQName("choose");

	QName STEP_COUNT = NAMESPACE_XPROC.newSaxonQName("count");

	QName STEP_FOR_EACH = NAMESPACE_XPROC.newSaxonQName("for-each");

	QName STEP_IDENTITY = NAMESPACE_XPROC.newSaxonQName("identity");

	QName STEP_LOAD = NAMESPACE_XPROC.newSaxonQName("load");

	QName STEP_OTHERWISE = NAMESPACE_XPROC.newSaxonQName("otherwise");

	QName STEP_PIPELINE = NAMESPACE_XPROC.newSaxonQName("pipeline");

	QName STEP_STORE = NAMESPACE_XPROC.newSaxonQName("store");

	QName STEP_WHEN = NAMESPACE_XPROC.newSaxonQName("when");

	QName STEP_XSL_FORMATTER = NAMESPACE_XPROC.newSaxonQName("xsl-formatter");

	QName STEP_XSLT = NAMESPACE_XPROC.newSaxonQName("xslt");
}
