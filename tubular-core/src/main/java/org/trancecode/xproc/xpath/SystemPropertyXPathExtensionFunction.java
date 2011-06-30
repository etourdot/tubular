/*
 * Copyright (C) 2010 Herve Quiroz
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.Tubular;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;

/**
 * {@code p:system-property()}.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#f.system-property">System
 *      Properties</a>
 */
public final class SystemPropertyXPathExtensionFunction extends AbstractXPathExtensionFunction
{
    private static final Logger LOG = Logger.getLogger(SystemPropertyXPathExtensionFunction.class);
    private static final Map<QName, String> PROPERTIES;
    private static final QName PROPERTY_EPISODE = XProcXmlModel.xprocNamespace().newSaxonQName("episode");

    static
    {
        final Map<QName, String> properties = Maps.newHashMap();
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("language"), Locale.getDefault().toString());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("product-name"), Tubular.productName());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("product-version"), Tubular.version());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("vendor"), Tubular.vendor());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("vendor-uri"), Tubular.vendorUri());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("version"), Tubular.xprocVersion());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("xpath-version"), Tubular.xpathVersion());
        properties.put(XProcXmlModel.xprocNamespace().newSaxonQName("psvi-supported"), "false");
        PROPERTIES = ImmutableMap.copyOf(properties);
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
                return XProcXmlModel.Functions.SYSTEM_PROPERTY;
            }

            @Override
            public int getMinimumNumberOfArguments()
            {
                return 1;
            }

            @Override
            public SequenceType[] getArgumentTypes()
            {
                return new SequenceType[] { SequenceType.SINGLE_STRING };
            }

            @Override
            public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
            {
                return SequenceType.SINGLE_STRING;
            }

            @Override
            public ExtensionFunctionCall makeCallExpression()
            {
                return new ExtensionFunctionCall()
                {
                    private static final long serialVersionUID = -8363336682570398286L;

                    @Override
                    public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                            throws XPathException
                    {
                        Preconditions.checkArgument(arguments.length == 1);
                        try
                        {
                            final QName property = resolveQName(arguments[0].next().getStringValue());
                            final String value;
                            if (property.equals(PROPERTY_EPISODE))
                            {
                                value = Environment.getCurrentEnvironment().getPipelineContext().getEpisode().getId();
                            }
                            else if (PROPERTIES.containsKey(property))
                            {
                                value = PROPERTIES.get(property);
                            }
                            else
                            {
                                value = "";
                            }
                            LOG.trace("{} = {}", property, value);
                            return SingletonIterator.makeIterator(StringValue.makeStringValue(value));
                        }
                        catch (final IllegalArgumentException e)
                        {
                            if (e.getCause() instanceof XPathException)
                            {
                                if ("FONS0004".equals(((XPathException) e.getCause()).getErrorCodeLocalPart()))
                                {
                                    throw XProcExceptions.xd0015(Environment.getCurrentEnvironment().getPipeline().getLocation());
                                }
                            }
                            else
                            {
                                e.printStackTrace();
                            }
                        }
                        return EmptyIterator.getInstance();
                    }
                };
            }
        };
    }
}
