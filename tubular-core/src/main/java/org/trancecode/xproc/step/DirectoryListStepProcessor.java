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

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import net.sf.saxon.s9api.QName;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * {@code p:directory-list}.
 * 
 * @author Emmanuel Tourdot
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.directory-list">p:directory-list</a>
 */
@ExternalResources(read = false, write = false)
public final class DirectoryListStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(DirectoryListStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.DIRECTORY_LIST;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final String path = input.getOptionValue(XProcOptions.PATH);
        assert path != null;
        LOG.trace("  path = {}", path);
        final String includeFilter = input.getOptionValue(XProcOptions.INCLUDE_FILTER);
        LOG.trace("  include-filter = {}", includeFilter);
        final String excludeFilter = input.getOptionValue(XProcOptions.EXCLUDE_FILTER);
        LOG.trace("  exclude-filter = {}", excludeFilter);

        final URI pathUri = input.getBaseUri().resolve(path);
        final File directory = new File(pathUri);
        if (!directory.canRead())
        {
            throw XProcExceptions.xc0012(input.getStep());
        }
        if (!directory.isDirectory())
        {
            throw XProcExceptions.xc0017(input.getStep());
        }
        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
        final ResultBuilder resultBuilder = new ResultBuilder(builder, includeFilter, excludeFilter);
        builder.startDocument();
        try
        {
            resultBuilder.start(directory);
        }
        catch (final IOException e)
        {
            throw XProcExceptions.xc0012(input.getStep());
        }
        builder.endDocument();
        output.writeNodes(XProcPorts.RESULT, builder.getNode());
    }

    class ResultBuilder extends DirectoryWalker
    {
        final SaxonBuilder builder;
        final String includeExpr;
        final String excludeExpr;

        public ResultBuilder(final SaxonBuilder builder, final String includeExpr, final String excludeExpr)
        {
            this.builder = builder;
            this.includeExpr = includeExpr;
            this.excludeExpr = excludeExpr;
        }

        @Override
        protected boolean handleDirectory(final File directory, final int depth, final Collection results)
                throws IOException
        {
            return depth == 0 || gotIt(directory);
        }

        @Override
        protected void handleDirectoryStart(final File directory, final int depth, final Collection results)
                throws IOException
        {
            builder.startElement(XProcXmlModel.Elements.DIRECTORY);
            builder.attribute(XProcXmlModel.Attributes.NAME, FilenameUtils.getName(directory.getAbsolutePath()));
        }

        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection results)
                throws IOException
        {
            builder.endElement();
        }

        @Override
        protected void handleFile(final File file, final int depth, final Collection results) throws IOException
        {
            if (gotIt(file))
            {
                if (file.isFile())
                {
                    builder.startElement(XProcXmlModel.Elements.FILE);
                }
                else
                {
                    builder.startElement(XProcXmlModel.Elements.OTHER);
                }
                builder.attribute(XProcXmlModel.Attributes.NAME, FilenameUtils.getName(file.getAbsolutePath()));
                builder.endElement();
            }
        }

        public void start(final File file) throws IOException
        {
            walk(file, Lists.newArrayList());
        }

        private boolean gotIt(final File file)
        {
            assert file != null;
            boolean gotIt = true;
            if (includeExpr != null)
            {
                gotIt = file.getName().matches(includeExpr);
            }
            if (excludeExpr != null)
            {
                gotIt = gotIt && !file.getName().matches(excludeExpr);
            }
            return gotIt;
        }
    }
}
