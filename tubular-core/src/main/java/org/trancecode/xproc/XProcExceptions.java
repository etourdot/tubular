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

import java.net.URI;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.xml.Location;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.XProcException.Type;
import org.trancecode.xproc.port.PortReference;
import org.trancecode.xproc.step.Step;
import org.trancecode.xproc.variable.Variable;

/**
 * @author Herve Quiroz
 */
public final class XProcExceptions
{
    private XProcExceptions()
    {
        // No instantiation
    }

    public static XProcException xd0004(final Location location)
    {
        return newXProcException(Type.DYNAMIC, 4, location,
                "no subpipeline is selected by the p:choose and no default is provided");
    }

    public static XProcException xd0006(final Location location, final PortReference port)
    {
        return newXProcException(Type.DYNAMIC, 6, location, "wrong number of input documents in port %s", port);
    }

    public static XProcException xd0023(final Location location, final String select, final String errorMessage)
    {
        return newXProcException(Type.DYNAMIC, 23, location, "XPath expression cannot be evaluated: %s\n%s", select,
                errorMessage);
    }

    public static XProcException xd0034(final Location location)
    {
        return newXProcException(
                Type.DYNAMIC,
                34,
                location,
                "It is a dynamic error to specify a new namespace or prefix if the lexical value of the specified name contains a colon (or if no wrapper is explicitly specified).");
    }

    public static XProcException xs0018(final Variable option)
    {
        return newXProcException(Type.STATIC, 18, option.getLocation(), "Option %s is required and is missing a value",
                option.getName());
    }

    public static XProcException xs0031(final Location location, final QName optionName, final QName stepType)
    {
        return newXProcException(Type.STATIC, 31, location, "Option %s is not declared on this step type (%s)",
                optionName, stepType);
    }

    public static XProcException xs0044(final XdmNode element)
    {
        return newXProcException(Type.STATIC, 44, SaxonLocation.of(element), "Unsupported element: %s",
                element.getNodeName());
    }

    public static XProcException xs0052(final Location location, final URI uri)
    {
        return newXProcException(
                Type.STATIC,
                52,
                location,
                "%s cannot be retrieved or if, once retrieved, it does not point to a p:library, p:declare-step, or p:pipeline",
                uri);
    }

    public static XProcException xc0019(final Step step)
    {
        return newXProcException(Type.STEP, 19, step.getLocation(), "Documents are not equal in step %s",
                step.getName());
    }

    public static XProcException xc0023(final XdmNode node, final XdmNodeKind... allowedNodeTypes)
    {
        return newXProcException(Type.STEP, 23, SaxonLocation.of(node),
                "Selected node type %s is not allowed by the step ; allowed types: %s ", node.getNodeKind(),
                allowedNodeTypes);
    }

    public static XProcException xc0038(final Location location, final String version)
    {
        return newXProcException(Type.STEP, 38, location, "XSLT version %s not supported", version);
    }

    public static XProcException xc0059(final Location location)
    {
        return newXProcException(
                Type.STEP,
                59,
                location,
                "It is a dynamic error if the QName value in the attribute-name option uses the prefix \"xmlns\" or any other prefix that resolves to the namespace name \"http://www.w3.org/2000/xmlns/\".");
    }

    public static XProcException xc0062(final Location location, final XdmNode node)
    {
        return newXProcException(Type.STEP, 62, location, "the match option matches a namespace node: %s",
                node.getNodeName());
    }

    private static XProcException newXProcException(final Type type, final int code, final Location location,
            final String message, final Object... parameters)
    {
        final XProcException exception = new XProcException(type, code, location, message, parameters);
        final StackTraceElement[] stackTrace = new StackTraceElement[exception.getStackTrace().length - 1];
        System.arraycopy(exception.getStackTrace(), 1, stackTrace, 0, stackTrace.length);
        exception.setStackTrace(stackTrace);
        return exception;
    }
}
