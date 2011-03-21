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
 */
package org.trancecode.xproc.xpath;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceType;
import org.trancecode.lang.TcStrings;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.XProcXmlModel;

public final class BaseUriXPathExtensionFunction extends AbstractXPathExtensionFunction
{
    private static final Logger LOG = Logger.getLogger(BaseUriXPathExtensionFunction.class);

    @Override
    public ExtensionFunctionDefinition getExtensionFunctionDefinition()
    {
        return new ExtensionFunctionDefinition()
        {private static final long serialVersionUID = 5111101376264111478L;

            @Override
            public StructuredQName getFunctionQName()
            {
                return XProcXmlModel.Functions.BASE_URI;
            }

            @Override
            public int getMinimumNumberOfArguments()
            {
                return 0;
            }

            @Override
            public int getMaximumNumberOfArguments()
            {
                return 1;
            }

            @Override
            public SequenceType[] getArgumentTypes()
            {
                return new SequenceType[] { SequenceType.OPTIONAL_NODE };
            }

            @Override
            public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
            {
                return SequenceType.SINGLE_ATOMIC;
            }

            @Override
            public ExtensionFunctionCall makeCallExpression()
            {
                return new ExtensionFunctionCall()
                {private static final long serialVersionUID = -5219886632773617494L;

                    @Override
                    public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                            throws XPathException
                    {
                        final NodeInfo nodeInfo;
                        if (arguments.length == 0)
                        {
                            nodeInfo = (NodeInfo) context.getContextItem();
                        }
                        else
                        {
                            nodeInfo = (NodeInfo) arguments[0].next();
                        }
                        final String baseUri = TcStrings.toString(nodeInfo.getBaseURI());
                        LOG.trace("baseUri = {}", baseUri);

                        return SingletonIterator.makeIterator(new AnyURIValue(baseUri));
                    }
                };
            }
        };
    }
}
