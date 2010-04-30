/*
 * Copyright (C) 2010 Romain Deltour
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
 */
package org.trancecode.xproc;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/**
 * Represents an XProc test description.
 * @author Romain Deltour
 */
public class XProcTestCase
{

    private final String title;
    private final XdmNode description;
    private final boolean ignoreWhitespace;
    private final XdmNode pipeline;
    private final Map<String, List<XdmNode>> inputs;
    private final Map<QName, String> options;
    private final Map<String, Map<QName, String>> parameters;
    private final QName error;
    private final Map<String, List<XdmNode>> outputs;
    private final XdmNode comparePipeline;

    public XProcTestCase(final String title, final XdmNode description, final boolean ignoreWhitespace,
            final XdmNode pipeline, final Map<String, List<XdmNode>> inputs, final Map<QName, String> options,
            final Map<String, Map<QName, String>> parameters, final QName error,
            final Map<String, List<XdmNode>> outputs, final XdmNode comparePipeline)
    {
        super();
        this.title = title;
        this.description = description;
        this.ignoreWhitespace = ignoreWhitespace;
        this.pipeline = pipeline;
        this.inputs = ImmutableMap.copyOf(inputs);
        this.options = ImmutableMap.copyOf(options);
        this.parameters = ImmutableMap.copyOf(parameters);
        this.error = error;
        this.outputs = ImmutableMap.copyOf(outputs);
        this.comparePipeline = comparePipeline;
    }

    public String getTitle()
    {
        return this.title;
    }

    public XdmNode getDescription()
    {
        return this.description;
    }

    public boolean isIgnoreWhitespace()
    {
        return this.ignoreWhitespace;
    }

    public XdmNode getPipeline()
    {
        return this.pipeline;
    }

    public Map<String, List<XdmNode>> getInputs()
    {
        return this.inputs;
    }

    public Map<QName, String> getOptions()
    {
        return this.options;
    }

    public Map<String, Map<QName, String>> getParameters()
    {
        return this.parameters;
    }

    public QName getError()
    {
        return this.error;
    }

    public Map<String, List<XdmNode>> getOutputs()
    {
        return this.outputs;
    }

    public XdmNode getComparePipeline()
    {
        return this.comparePipeline;
    }

}
