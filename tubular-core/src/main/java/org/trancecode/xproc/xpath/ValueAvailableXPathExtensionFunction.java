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
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcXmlModel;

/**
 * {@code p:value-available()}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#f.value-available">Value
 *      Available</a>
 */
public final class ValueAvailableXPathExtensionFunction extends AbstractXPathExtensionFunction
{
    private static final Logger LOG = Logger.getLogger(ValueAvailableXPathExtensionFunction.class);

    @Override
    public ExtensionFunctionDefinition getExtensionFunctionDefinition()
    {
        return new ExtensionFunctionDefinition()
        {
            private static final long serialVersionUID = -5180297083383673166L;

            @Override
            public StructuredQName getFunctionQName()
            {
                return XProcXmlModel.Functions.VALUE_AVAILABLE;
            }

            @Override
            public int getMinimumNumberOfArguments()
            {
                return 1;
            }

            @Override
            public int getMaximumNumberOfArguments()
            {
                return 2;
            }

            @Override
            public SequenceType[] getArgumentTypes()
            {
                return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_BOOLEAN };
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
                    private static final long serialVersionUID = -3880313176590813975L;

                    @Override
                    public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                            throws XPathException
                    {
                        final QName name = resolveQName(arguments[0].next().getStringValue());
                        LOG.trace("{@method} name = {}", name);
                        final boolean available = Environment.getCurrentEnvironment().getVariable(name) != null;
                        LOG.trace("  available = {}", available);
                        return SingletonIterator.makeIterator(BooleanValue.get(available));
                    }
                };
            }
        };
    }
}
