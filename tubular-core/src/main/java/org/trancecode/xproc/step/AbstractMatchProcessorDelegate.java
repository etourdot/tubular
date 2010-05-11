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
package org.trancecode.xproc.step;

import java.util.EnumSet;

import net.sf.saxon.s9api.XdmNode;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xml.saxon.SaxonProcessorDelegate;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.XProcExceptions;

/**
 * Abstract processor delegate that implements all the
 * {@link SaxonProcessorDelegate} methods by throwing an <code>err:XC0023</code>
 * error: <i>It is a dynamic error if a select expression or match pattern
 * returns a node type that is not allowed by the step</i>.
 * 
 * @author Romain Deltour
 * @see XProcExceptions
 */
public abstract class AbstractMatchProcessorDelegate implements SaxonProcessorDelegate
{

    private final Step step;

    public AbstractMatchProcessorDelegate(final Step step)
    {
        this.step = step;
    }

    @Override
    public void attribute(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

    @Override
    public void comment(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

    @Override
    public void endDocument(final XdmNode node, final SaxonBuilder builder)
    {
        builder.endDocument();
    }

    @Override
    public void endElement(final XdmNode node, final SaxonBuilder builder)
    {
        builder.endElement();
    }

    @Override
    public void processingInstruction(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

    @Override
    public boolean startDocument(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

    @Override
    public EnumSet<NextSteps> startElement(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

    @Override
    public void text(final XdmNode node, final SaxonBuilder builder)
    {
        throw XProcExceptions.xc0023(step.getLocation());
    }

}
