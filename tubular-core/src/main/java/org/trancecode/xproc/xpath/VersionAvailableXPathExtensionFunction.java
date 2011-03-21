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
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.SequenceType;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.XProcXmlModel;

public final class VersionAvailableXPathExtensionFunction extends AbstractXPathExtensionFunction
{
    private static final Logger LOG = Logger.getLogger(VersionAvailableXPathExtensionFunction.class);

    @Override
    public ExtensionFunctionDefinition getExtensionFunctionDefinition()
    {
        return new ExtensionFunctionDefinition()
        {
            private static final long serialVersionUID = 1058896158728072862L;

            @Override
            public StructuredQName getFunctionQName()
            {
                return XProcXmlModel.Functions.VERSION_AVAILABLE;
            }

            @Override
            public int getMinimumNumberOfArguments()
            {
                return 1;
            }

            @Override
            public int getMaximumNumberOfArguments()
            {
                return 1;
            }

            @Override
            public SequenceType[] getArgumentTypes()
            {
                return new SequenceType[] { SequenceType.SINGLE_DOUBLE };
            }

            @Override
            public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
            {
                return SequenceType.SINGLE_BOOLEAN;
            }

            @Override
            public ExtensionFunctionCall makeCallExpression()
            {
                return new ExtensionFunctionCall()
                {
                    private static final long serialVersionUID = -4814962705491172016L;

                    @Override
                    public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                            throws XPathException
                    {
                        final double version = ((DoubleValue) arguments[0].next()).getDoubleValue();
                        LOG.trace("version = {}", version);
                        return SingletonIterator.makeIterator(version == 1.0 ? BooleanValue.TRUE : BooleanValue.FALSE);
                    }
                };
            }
        };
    }
}
