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
package org.trancecode.xproc.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Herve Quiroz
 */
public final class TubularBundleActivator implements BundleActivator
{
    private ServiceRegistration<PipelineService> serviceRegistration;

    @Override
    public void start(final BundleContext context)
    {
        serviceRegistration = context.registerService(PipelineService.class, new TubularPipelineService(), null);
    }

    @Override
    public void stop(final BundleContext context)
    {
        context.ungetService(serviceRegistration.getReference());
    }
}
