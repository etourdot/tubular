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
package org.trancecode.xproc.jaxproc;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.trancecode.xproc.PipelineConfiguration;
import org.trancecode.xproc.PipelineProcessor;
import org.trancecode.xproc.Tubular;
import org.trancecode.xproc.api.Pipeline;
import org.trancecode.xproc.api.PipelineFactory;
import org.trancecode.xproc.api.XProcProperties;

/**
 * @author Herve Quiroz
 */
public final class TubularPipelineFactory extends PipelineFactory
{
    @Override
    public Pipeline newPipeline(final Source pipelineSource)
    {
        final PipelineConfiguration configuration = new PipelineConfiguration();
        final URIResolver uriResolver = (URIResolver) getProperties().get(XProcProperties.URI_RESOLVER);
        if (uriResolver != null)
        {
            configuration.setUriResolver(uriResolver);
        }

        final PipelineProcessor processor = new PipelineProcessor(configuration);
        return new TubularPipeline(processor.buildPipeline(pipelineSource).load());
    }

    @Override
    public String getVersion()
    {
        return Tubular.version();
    }

    @Override
    public String getXProcVersion()
    {
        return Tubular.xprocVersion();
    }

    @Override
    public String getXPathVersion()
    {
        return Tubular.xpathVersion();
    }

    @Override
    public String getProductName()
    {
        return Tubular.productName();
    }

    @Override
    public String getVendor()
    {
        return Tubular.vendor();
    }

    @Override
    public String getVendorUri()
    {
        return Tubular.vendorUri();
    }
}
