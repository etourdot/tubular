/*
 * Copyright (C) 2011 Emmanuel Tourdot
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
package org.trancecode.xproc.step;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonLocation;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;

/**
 * {@code p:xquery}.
 * 
 * @author Emmanuel Tourdot
 * @see <a href="http://www.w3.org/TR/xproc/#c.xquery">p:xquery</a>
 */
@ExternalResources(read = false, write = false)
public final class XQueryStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(XQueryStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.XQUERY;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final Iterable<XdmNode> sourcesDoc = readSequencePort(input, XProcPorts.SOURCE);
        final XdmNode queryNode = input.readNode(XProcPorts.QUERY);
        LOG.trace("query = {}", queryNode.getStringValue());

        final Processor processor = input.getPipelineContext().getProcessor();
        final XQueryCompiler xQueryCompiler = processor.newXQueryCompiler();
        try
        {
            final XQueryEvaluator xQueryEvaluator = xQueryCompiler.compile(queryNode.getStringValue()).load();
            xQueryEvaluator.setContextItem(Iterables.getFirst(sourcesDoc, null));
            final Iterator<XdmItem> iterator = xQueryEvaluator.iterator();
            while (iterator.hasNext())
            {
                final XdmItem item = iterator.next();
                if (item.isAtomicValue())
                {
                    throw XProcExceptions.xc0057(SaxonLocation.of(queryNode));
                }
                output.writeNodes(XProcPorts.RESULT, (XdmNode) item);
            }
        }
        catch (SaxonApiException e)
        {
            e.printStackTrace();
        }
    }

    private Iterable<XdmNode> readSequencePort(final StepInput input, final String portName)
    {
        final Iterable<XdmNode> source = input.readNodes(portName);
        final Iterator<XdmNode> iterator = source.iterator();
        final ImmutableList.Builder builder = new ImmutableList.Builder();
        while (iterator.hasNext())
        {
            final XdmNode node = iterator.next();
            if (XdmNodeKind.DOCUMENT.equals(node.getNodeKind()))
            {
                builder.addAll(SaxonAxis.childNodes(node));
            }
            else
            {
                builder.add(node);
            }
        }
        return builder.build();
    }
}
