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

import org.trancecode.xproc.XProcNamespaces;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;


/**
 * Elements from the XProc XML model.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcElements
{
	QName ELEMENT_CHOOSE = XProcNamespaces.XPROC.newSaxonQName("choose");
	QName ELEMENT_DECLARE_STEP = XProcNamespaces.XPROC.newSaxonQName("declare-step");
	QName ELEMENT_DOCUMENT = XProcNamespaces.XPROC.newSaxonQName("document");
	QName ELEMENT_EMPTY = XProcNamespaces.XPROC.newSaxonQName("empty");
	QName ELEMENT_FOR_EACH = XProcNamespaces.XPROC.newSaxonQName("for-each");
	QName ELEMENT_IMPORT = XProcNamespaces.XPROC.newSaxonQName("import");
	QName ELEMENT_INLINE = XProcNamespaces.XPROC.newSaxonQName("inline");
	QName ELEMENT_INPUT = XProcNamespaces.XPROC.newSaxonQName("input");
	QName ELEMENT_ITERATION_SOURCE = XProcNamespaces.XPROC.newSaxonQName("iteration-source");
	QName ELEMENT_LIBRARY = XProcNamespaces.XPROC.newSaxonQName("library");
	QName ELEMENT_OPTION = XProcNamespaces.XPROC.newSaxonQName("option");
	QName ELEMENT_OTHERWISE = XProcNamespaces.XPROC.newSaxonQName("otherwise");
	QName ELEMENT_OUTPUT = XProcNamespaces.XPROC.newSaxonQName("output");
	QName ELEMENT_PIPE = XProcNamespaces.XPROC.newSaxonQName("pipe");
	QName ELEMENT_PIPELINE = XProcNamespaces.XPROC.newSaxonQName("pipeline");
	QName ELEMENT_VARIABLE = XProcNamespaces.XPROC.newSaxonQName("variable");
	QName ELEMENT_WHEN = XProcNamespaces.XPROC.newSaxonQName("when");
	QName ELEMENT_WITH_OPTION = XProcNamespaces.XPROC.newSaxonQName("with-option");
	QName ELEMENT_WITH_PARAM = XProcNamespaces.XPROC.newSaxonQName("with-param");
	QName ELEMENT_XPATH_CONTEXT = XProcNamespaces.XPROC.newSaxonQName("xpath-context");

	Set<QName> ELEMENTS_CORE_STEPS = ImmutableSet.of(ELEMENT_FOR_EACH, ELEMENT_CHOOSE);
	Set<QName> ELEMENTS_DECLARE_STEP_OR_PIPELINE = ImmutableSet.of(ELEMENT_DECLARE_STEP, ELEMENT_PIPELINE);
	Set<QName> ELEMENTS_IN_PIPELINE = ImmutableSet.of(ELEMENT_IMPORT, ELEMENT_PIPELINE);
	Set<QName> ELEMENTS_IN_PIPELINE_LIBRARY = ImmutableSet.of(ELEMENT_IMPORT, ELEMENT_DECLARE_STEP, ELEMENT_PIPELINE);
	Set<QName> ELEMENTS_PORT_BINDINGS = ImmutableSet.of(ELEMENT_INLINE, ELEMENT_DOCUMENT, ELEMENT_EMPTY, ELEMENT_PIPE);
	Set<QName> ELEMENTS_INPUT_PORTS = ImmutableSet.of(ELEMENT_INPUT, ELEMENT_ITERATION_SOURCE, ELEMENT_XPATH_CONTEXT);
	Set<QName> ELEMENTS_OUTPUT_PORTS = ImmutableSet.of(ELEMENT_OUTPUT);
	Set<QName> ELEMENTS_PORTS = ImmutableSet.copyOf(Iterables.concat(ELEMENTS_INPUT_PORTS, ELEMENTS_OUTPUT_PORTS));
	Set<QName> ELEMENTS_VARIABLES =
		ImmutableSet.of(ELEMENT_VARIABLE, ELEMENT_OPTION, ELEMENT_WITH_OPTION, ELEMENT_WITH_PARAM);
	Set<QName> ELEMENTS_STANDARD_PORTS = ImmutableSet.of(ELEMENT_INPUT, ELEMENT_OUTPUT);
	Set<QName> ELEMENTS_ROOT = ImmutableSet.of(ELEMENT_PIPELINE, ELEMENT_LIBRARY, ELEMENT_DECLARE_STEP);
	Set<QName> ELEMENTS_WHEN_OTHERWISE = ImmutableSet.of(ELEMENT_WHEN, ELEMENT_OTHERWISE);

}
