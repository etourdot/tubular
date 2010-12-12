/*
 * Copyright (C) 2010 TranceCode Software
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
package org.trancecode.xproc.step;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.trancecode.core.StringPredicates;
import org.trancecode.core.TcStrings;
import org.trancecode.io.TcByteStreams;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.SaxonAxis;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.Environment;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.XProcXmlModel;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;

/**
 * Step processor for the p:exec optional XProc step.
 * 
 * @author Herve Quiroz
 * @see <a href="http://www.w3.org/TR/xproc/#c.exec">p:exec</a>
 */
public final class ExecStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(AddAttributeStepProcessor.class);

    @Override
    public QName stepType()
    {
        return XProcSteps.EXEC;
    }

    @Override
    protected Environment doRun(final Step step, final Environment environment) throws Exception
    {
        LOG.trace("{@method} step = {}", step.getName());

        final String command = environment.getVariable(XProcOptions.COMMAND);
        final String argSeparator = environment.getVariable(XProcOptions.ARG_SEPARATOR, " ");
        final Iterable<String> args = TcStrings.split(environment.getVariable(XProcOptions.ARGS), argSeparator);
        final String cwd = environment.getVariable(XProcOptions.CWD);

        final List<XdmNode> inputDocuments = ImmutableList.copyOf(environment.readNodes(step
                .getPortReference(XProcPorts.SOURCE)));
        if (inputDocuments.size() > 1)
        {
            throw XProcExceptions.xd0006(step.getLocation(), step.getPortReference(XProcPorts.SOURCE));
        }

        final List<String> commandLine = Lists.newArrayList();
        commandLine.add(command);
        Iterables.addAll(commandLine, Iterables.filter(args, StringPredicates.isNotEmpty()));
        LOG.trace("commandLine = {}", commandLine);
        final ProcessBuilder processBuilder = new ProcessBuilder(commandLine.toArray(new String[0]));
        processBuilder.redirectErrorStream(true);
        if (cwd != null)
        {
            processBuilder.directory(new File(cwd));
        }
        final Process process = processBuilder.start();

        if (!inputDocuments.isEmpty())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        IOUtils.write(inputDocuments.get(0).toString(), process.getOutputStream());
                    }
                    catch (final IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                    finally
                    {
                        Closeables.closeQuietly(process.getOutputStream());
                    }
                }
            }).start();
        }

        final Supplier<File> stdout = TcByteStreams.copyToTempFile(process.getInputStream());
        final Supplier<File> stderr = TcByteStreams.copyToTempFile(process.getErrorStream());

        final int exitCode = process.waitFor();
        final File stdoutFile = stdout.get();
        final File stderrFile = stderr.get();
        process.destroy();

        final SaxonBuilder builder = new SaxonBuilder(environment.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
        builder.startDocument();
        builder.startElement(XProcXmlModel.Elements.RESULT, step.getNode());
        final boolean isResultXml = Boolean.parseBoolean(environment.getVariable(XProcOptions.RESULT_IS_XML));
        if (isResultXml)
        {
            final XdmNode resultNode = environment.getPipelineContext().getProcessor().newDocumentBuilder()
                    .build(stdoutFile);
            builder.nodes(SaxonAxis.childElement(resultNode));
        }
        else
        {
            final boolean wrapResultLines = Boolean.parseBoolean(environment
                    .getVariable(XProcOptions.WRAP_RESULT_LINEs));
            if (wrapResultLines)
            {
                @SuppressWarnings("unchecked")
                final List<String> lines = FileUtils.readLines(stdoutFile);
                for (final String line : lines)
                {
                    builder.startElement(XProcXmlModel.Elements.LINE);
                    builder.text(line);
                    builder.endElement();
                }
            }
            else
            {
                builder.text(FileUtils.readFileToString(stdoutFile));
            }
        }

        builder.endElement();
        builder.endDocument();

        return environment.writeNodes(step.getPortReference(XProcPorts.RESULT), builder.getNode());
    }
}
