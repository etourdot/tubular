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
package org.trancecode.xproc.parser;

import org.trancecode.xml.XmlModel;
import org.trancecode.xproc.XProcNamespaces;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import net.sf.saxon.s9api.QName;


/**
 * Namespaces, elements and attributes from the XProc XML model.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcXmlModel extends XmlModel
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

	QName ELEMENT_CHOOSE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("choose");

	QName ELEMENT_DECLARE_STEP = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("declare-step");

	QName ELEMENT_DOCUMENT = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("document");

	QName ELEMENT_EMPTY = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("empty");

	QName ELEMENT_FOR_EACH = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("for-each");

	QName ELEMENT_IMPORT = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("import");

	QName ELEMENT_INLINE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("inline");

	QName ELEMENT_INPUT = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("input");

	QName ELEMENT_ITERATION_SOURCE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("iteration-source");

	QName ELEMENT_LIBRARY = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("library");

	QName ELEMENT_OPTION = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("option");

	QName ELEMENT_OTHERWISE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("otherwise");

	QName ELEMENT_OUTPUT = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("output");

	QName ELEMENT_PIPE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("pipe");

	QName ELEMENT_PIPELINE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("pipeline");

	QName ELEMENT_VARIABLE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("variable");

	QName ELEMENT_WHEN = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("when");

	QName ELEMENT_WITH_OPTION = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("with-option");

	QName ELEMENT_WITH_PARAM = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("with-param");

	QName ELEMENT_XPATH_CONTEXT = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("xpath-context");

	Set<QName> ELEMENTS_CORE_STEPS = ImmutableSet.of(ELEMENT_FOR_EACH, ELEMENT_CHOOSE);

	Set<QName> ELEMENTS_DECLARE_STEP_OR_PIPELINE = ImmutableSet.of(ELEMENT_DECLARE_STEP, ELEMENT_PIPELINE);

	Set<QName> ELEMENTS_IN_PIPELINE = ImmutableSet.of(ELEMENT_IMPORT, ELEMENT_PIPELINE);

	Set<QName> ELEMENTS_IN_PIPELINE_LIBRARY = ImmutableSet.of(ELEMENT_IMPORT, ELEMENT_DECLARE_STEP, ELEMENT_PIPELINE);

	Set<QName> ELEMENTS_PORT_BINDINGS = ImmutableSet.of(ELEMENT_INLINE, ELEMENT_DOCUMENT, ELEMENT_EMPTY, ELEMENT_PIPE);

	Set<QName> ELEMENTS_INPUT_PORTS = ImmutableSet.of(ELEMENT_INPUT, ELEMENT_ITERATION_SOURCE, ELEMENT_XPATH_CONTEXT);

	Set<QName> ELEMENTS_OUTPUT_PORTS = ImmutableSet.of(ELEMENT_OUTPUT);

	Set<QName> ELEMENTS_PORTS = ImmutableSet.copyOf(Iterables.concat(ELEMENTS_INPUT_PORTS, ELEMENTS_OUTPUT_PORTS));

	Set<QName> ELEMENTS_STANDARD_PORTS = ImmutableSet.of(ELEMENT_INPUT, ELEMENT_OUTPUT);

	Set<QName> ELEMENTS_ROOT = ImmutableSet.of(ELEMENT_PIPELINE, ELEMENT_LIBRARY, ELEMENT_DECLARE_STEP);

	Set<QName> ELEMENTS_WHEN_OTHERWISE = ImmutableSet.of(ELEMENT_WHEN, ELEMENT_OTHERWISE);

	QName TYPE_PIPELINE = XProcNamespaces.NAMESPACE_XPROC.newSaxonQName("pipeline");
}
