/*
 * Copyright (C) 2011 Herve Quiroz
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
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import org.trancecode.lang.TcThreads;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.XProcXmlModel;

public final class IterationSizeXPathExtensionFunction extends AbstractXPathExtensionFunction
{
    private static final Logger LOG = Logger.getLogger(IterationSizeXPathExtensionFunction.class);
    private static final ThreadLocal<Integer> ITERATION_SIZE = new ThreadLocal<>();

    static
    {
        ITERATION_SIZE.set(1);
    }

    public static int setIterationSize(final int value)
    {
        final int previousValue = TcThreads.set(ITERATION_SIZE, value);
        LOG.trace("{@method} {} -> {}", previousValue, value);
        return previousValue;
    }

    @Override
    public ExtensionFunctionDefinition getExtensionFunctionDefinition()
    {
        return new ExtensionFunctionDefinition()
        {
            private static final long serialVersionUID = -2376250179411225176L;

            @Override
            public StructuredQName getFunctionQName()
            {
                return XProcXmlModel.Functions.ITERATION_SIZE;
            }

            @Override
            public int getMinimumNumberOfArguments()
            {
                return 0;
            }

            @Override
            public SequenceType[] getArgumentTypes()
            {
                return new SequenceType[0];
            }

            @Override
            public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
            {
                return SequenceType.SINGLE_INTEGER;
            }

            @Override
            public ExtensionFunctionCall makeCallExpression()
            {
                return new ExtensionFunctionCall()
                {
                    private static final long serialVersionUID = -8363336682570398286L;

                    /*@Override
                    public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                            throws XPathException
                    {
                        return SingletonIterator.makeIterator(Int64Value.makeIntegerValue(ITERATION_SIZE.get()));
                    }*/

                    @Override
                    public Sequence call(XPathContext xPathContext, Sequence[] sequences) throws XPathException {
                        return Int64Value.makeIntegerValue(ITERATION_SIZE.get());
                    }
                };
            }
        };
    }
}
