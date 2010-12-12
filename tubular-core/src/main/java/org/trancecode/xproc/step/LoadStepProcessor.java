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
package org.trancecode.xproc.step;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xml.Jaxp;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * @author Herve Quiroz
 */
public final class LoadStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(LoadStepProcessor.class);

    @Override
    public QName stepType()
    {
        return XProcSteps.LOAD;
    }

    @Override
    protected Environment doRun(final Step step, final Environment environment)
    {
        LOG.trace("step = {}", step.getName());
        assert step.getType().equals(XProcSteps.LOAD);

        final String href = environment.getVariable(XProcOptions.HREF);
        assert href != null;
        LOG.trace("href = {}", href);

        final boolean validate = Boolean.parseBoolean(environment.getVariable(XProcOptions.VALIDATE));
        LOG.trace("validate = {}", validate);

        final Source source;
        try
        {
            source = environment.getPipelineContext().getUriResolver()
                    .resolve(href, environment.getBaseUri().toString());
        }
        catch (final Exception e)
        {
            throw new PipelineException("Error while trying to read document ; href = %s ; baseUri = %s", e, href,
                    environment.getBaseUri());
        }

        final XdmNode document;
        try
        {
            document = environment.getPipelineContext().getProcessor().newDocumentBuilder().build(source);
        }
        catch (final SaxonApiException e)
        {
            throw new PipelineException("Error while trying to build document ; href = %s", e, href);
        }
        finally
        {
            Jaxp.closeQuietly(source, LOG);
        }

        return environment.writeNodes(step.getPortReference(XProcPorts.RESULT), document);
    }
}
