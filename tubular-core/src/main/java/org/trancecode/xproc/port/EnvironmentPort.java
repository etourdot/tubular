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
package org.trancecode.xproc.port;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Location;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.binding.EnvironmentPortBinding;
import org.trancecode.xproc.binding.PortBinding;

/**
 * @author Herve Quiroz
 */
public class EnvironmentPort implements HasPortReference
{
    private static final Logger LOG = Logger.getLogger(EnvironmentPort.class);

    private final Port declaredPort;
    private final List<EnvironmentPortBinding> portBindings;
    private final XPathExecutable select;

    public static EnvironmentPort newEnvironmentPort(final Port declaredPort, final Environment environment)
    {
        assert declaredPort != null;
        assert environment != null;
        LOG.trace("declaredPort = {}", declaredPort);
        LOG.trace("portBindings = {}", declaredPort.getPortBindings());

        final List<EnvironmentPortBinding> portBindings = ImmutableList.copyOf(Iterables.transform(
                declaredPort.getPortBindings(), new Function<PortBinding, EnvironmentPortBinding>()
                {
                    @Override
                    public EnvironmentPortBinding apply(final PortBinding portBinding)
                    {
                        return portBinding.newEnvironmentPortBinding(environment);
                    }
                }));

        final String declaredPortSelect = declaredPort.getSelect();
        final XPathExecutable select;
        if (declaredPortSelect != null)
        {
            try
            {
                select = environment.getConfiguration().getProcessor().newXPathCompiler().compile(declaredPortSelect);
            }
            catch (final SaxonApiException e)
            {
                throw XProcExceptions.xd0023(declaredPort.getLocation(), declaredPortSelect, e.getMessage());
            }
        }
        else
        {
            select = null;
        }

        return new EnvironmentPort(declaredPort, portBindings, select);
    }

    private EnvironmentPort(final Port declaredPort, final Iterable<EnvironmentPortBinding> portBindings,
            final XPathExecutable select)
    {
        this.declaredPort = declaredPort;
        this.portBindings = ImmutableList.copyOf(portBindings);
        this.select = select;
    }

    public final List<EnvironmentPortBinding> portBindings()
    {
        return portBindings;
    }

    public Port getDeclaredPort()
    {
        return declaredPort;
    }

    public Iterable<XdmNode> readNodes()
    {
        LOG.trace("{@method} declaredPort = {}", declaredPort);

        // TODO improve this by returning a true Iterable
        final List<XdmNode> nodes = Lists.newArrayList();
        for (final EnvironmentPortBinding portBinding : portBindings)
        {
            for (final XdmNode node : portBinding.readNodes())
            {
                if (select != null)
                {
                    LOG.trace("select = {}", select);
                    try
                    {
                        final XPathSelector selector = select.load();
                        selector.setContextItem(node);
                        for (final XdmItem xdmItem : selector.evaluate())
                        {
                            nodes.add((XdmNode) xdmItem);
                        }
                    }
                    catch (final SaxonApiException e)
                    {
                        throw XProcExceptions.xd0023(declaredPort.getLocation(), declaredPort.getSelect(),
                                e.getMessage());
                    }
                }
                else
                {
                    nodes.add(node);
                }
            }
        }

        // defensive programming
        return ImmutableList.copyOf(nodes);
    }

    public EnvironmentPort writeNodes(final XdmNode... nodes)
    {
        return writeNodes(ImmutableList.copyOf(nodes));
    }

    public EnvironmentPort writeNodes(final Iterable<XdmNode> nodes)
    {
        assert portBindings.isEmpty();

        final List<XdmNode> nodeList = ImmutableList.copyOf(nodes);
        LOG.trace("{} nodes -> {}", nodeList.size(), declaredPort.portReference());
        final EnvironmentPortBinding portBinding = new EnvironmentPortBinding()
        {
            public Iterable<XdmNode> readNodes()
            {
                return nodeList;
            }

            @Override
            public Location getLocation()
            {
                return declaredPort.getLocation();
            }
        };

        return new EnvironmentPort(declaredPort, Collections.singleton(portBinding), select);
    }

    public EnvironmentPort pipe(final EnvironmentPort port)
    {
        assert port != null : getDeclaredPort();
        assert port != this : getDeclaredPort();
        LOG.trace("{} -> {}", port.getDeclaredPort(), getDeclaredPort());

        final EnvironmentPortBinding portBinding = new EnvironmentPortBinding()
        {
            public Iterable<XdmNode> readNodes()
            {
                LOG.trace("read from {}", port);
                return port.readNodes();
            }

            @Override
            public Location getLocation()
            {
                return declaredPort.getLocation();
            }
        };

        return new EnvironmentPort(declaredPort, Collections.singleton(portBinding), select);
    }

    @Override
    public PortReference portReference()
    {
        return getDeclaredPort().portReference();
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s/%s]", getClass().getSimpleName(), declaredPort.getStepName(),
                declaredPort.getPortName());
    }
}
