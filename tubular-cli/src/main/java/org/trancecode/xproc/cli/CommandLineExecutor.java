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
package org.trancecode.xproc.cli;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.trancecode.xproc.Configuration;
import org.trancecode.xproc.Pipeline;
import org.trancecode.xproc.PipelineProcessor;
import org.trancecode.xproc.PipelineResult;
import org.trancecode.xproc.RunnablePipeline;

/**
 * @author Herve Quiroz
 * @author Torsten Knodt
 */
public final class CommandLineExecutor
{
    public static void main(final String[] args)
    {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
        Logger.getRootLogger().setLevel(Level.INFO);

        final Options options = new Options();
        final Option portBindingOption = new Option("b", "port-binding", true,
                "Passes a port binding to the given XProc pipeline");
        final Option optionOption = new Option("o", "option", true, "Passes a option to the given XProc pipeline");
        final Option paramOption = new Option("p", "param", true, "Passes a parameter to the given XProc pipeline");
        final Option librariesOption = new Option("l", "library", true, "XProc pipeline library to load");
        final Option xplOption = new Option("x", "xpl", true, "XProc pipeline to load and run");
        final Option primaryInputPortOption = new Option("i", "input-port", true, "Passes the primary input port");
        final Option primaryOutputPortOption = new Option("r", "output-port", true, "Passes the primary output port");
        final Option helpOption = new Option("h", "help", false, "Print help");

        xplOption.setArgName("uri");
        xplOption.setRequired(true);
        xplOption.setType(URL.class);
        portBindingOption.setArgName("name=uri");
        portBindingOption.setArgs(2);
        portBindingOption.setValueSeparator('=');
        optionOption.setArgName("name=value");
        optionOption.setArgs(2);
        optionOption.setValueSeparator('=');
        paramOption.setArgName("name=value");
        paramOption.setArgs(2);
        paramOption.setValueSeparator('=');
        librariesOption.setArgName("uri");
        librariesOption.setArgs(Option.UNLIMITED_VALUES);
        librariesOption.setType(URL.class);
        primaryInputPortOption.setArgName("name=uri");
        primaryInputPortOption.setArgs(2);
        primaryInputPortOption.setValueSeparator('=');
        primaryOutputPortOption.setArgName("name=uri");
        primaryOutputPortOption.setArgs(2);
        primaryOutputPortOption.setValueSeparator('=');
        options.addOption(portBindingOption);
        options.addOption(optionOption);
        options.addOption(paramOption);
        options.addOption(xplOption);
        options.addOption(librariesOption);
        options.addOption(primaryInputPortOption);
        options.addOption(primaryOutputPortOption);
        options.addOption(helpOption);

        final GnuParser parser = new GnuParser();

        try
        {
            final CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption(helpOption.getOpt()))
            {
                printHelp(options);
                System.exit(0);
            }

            final Configuration configurationPipelineContext = new Configuration();
            final URIResolver uriResolver = configurationPipelineContext.getUriResolver();
            final PipelineProcessor pipelineProcessor = new PipelineProcessor(configurationPipelineContext);
            final String[] libraries = commandLine.getOptionValues(librariesOption.getOpt());

            if (libraries != null)
            {
                for (final String library : libraries)
                {
                    pipelineProcessor.buildPipelineLibrary(optionValueToSource(uriResolver, library));
                }
            }

            // configurationPipelineContext.registerStepProcessor(null);
            final String xplValue = commandLine.getOptionValue(xplOption.getOpt());

            if (xplValue == null)
            {
                System.err.println("Required pipeline given using the --" + xplOption.getLongOpt() + " option.");
                printHelp(options);
                System.exit(2);
            }
            else
            {
                final Source xplSource = optionValueToSource(uriResolver, xplValue);

                if (xplSource != null)
                {
                    final Pipeline buildPipeline = pipelineProcessor.buildPipeline(xplSource);
                    final RunnablePipeline runnablePipeline = buildPipeline.load();

                    final Properties portBindingProperties = commandLine.getOptionProperties(optionOption.getOpt());
                    for (final String portBindingName : portBindingProperties.stringPropertyNames())
                    {
                        final String portBindingValue = portBindingProperties.getProperty(portBindingName);
                        runnablePipeline.setPortBinding(portBindingName,
                                optionValueToSource(uriResolver, portBindingValue));
                    }

                    final String primaryInputPortValue = commandLine.getOptionValue(primaryInputPortOption.getOpt());
                    if (primaryInputPortValue != null)
                    {
                        final Source primaryInputSource = optionValueToSource(uriResolver, primaryInputPortValue);
                        if (primaryInputSource != null)
                        {
                            runnablePipeline.setPortBinding(runnablePipeline.getPipeline().getPrimaryInputPort()
                                    .getPortName(), primaryInputSource);
                        }
                    }

                    final String primaryOutputPortValue = commandLine.getOptionValue(primaryOutputPortOption.getOpt());
                    // TODO TK: final Result resolve =
                    // configurationPipelineContext.getProcessor().getUnderlyingConfiguration().getOutputURIResolver().resolve(primaryOutputPortValue,
                    // null);

                    final Properties optionProperties = commandLine.getOptionProperties(optionOption.getOpt());
                    for (final String optionName : optionProperties.stringPropertyNames())
                    {
                        final String optionValue = optionProperties.getProperty(optionName);
                        runnablePipeline.withOption(new QName(optionName), optionValue);
                    }

                    final Properties paramProperties = commandLine.getOptionProperties(paramOption.getOpt());
                    for (final String paramName : paramProperties.stringPropertyNames())
                    {
                        final String paramValue = paramProperties.getProperty(paramName);
                        runnablePipeline.withParam(new QName(paramName), paramValue);
                    }

                    final PipelineResult pipelineResult = runnablePipeline.run();
                    final Serializer serializer = new Serializer();
                    serializer.setOutputStream(System.out); // TODO Use primary
                                                            // output port
                                                            // binding option
                    for (final XdmNode xdmNode : pipelineResult.readNodes(pipelineResult.getPipeline()
                            .getPrimaryOutputPort().getPortName()))
                    {
                        try
                        {
                            configurationPipelineContext.getProcessor().writeXdmValue(xdmNode, serializer);
                        }
                        catch (final SaxonApiException ex)
                        {
                            // FIXME
                            // Logger.getLogger(CommandLineExecutor.class.getName()).log(Level.SEVERE,
                            // null, ex);
                        }
                    }
                    System.out.println();
                }
                else
                {
                    System.err.println("Argument given to option --xpl is neither a URL nor or a file.");
                    printHelp(options);
                    System.exit(3);
                }
            }
        }
        catch (final ParseException ex)
        {
            printHelp(options);
            System.exit(1);
        }
    }

    private static Source optionValueToSource(final URIResolver uriResolver, final String portParamValue)
    {
        Source source = null;
        try
        {
            source = uriResolver.resolve(portParamValue, null);
        }
        catch (final TransformerException ex)
        {
            // Ignore, no URI, probably a file name
        }
        if (source == null)
        {
            final File file = new File(portParamValue);
            try
            {
                source = uriResolver.resolve(file.toURI().toString(), null);
            }
            catch (final TransformerException ex)
            {
                // Neither URI nor file name
            }
        }
        return source;
    }

    private static void printHelp(final Options options)
    {
        final HelpFormatter helpFormatter = new HelpFormatter();
        final PrintWriter printWriter = new PrintWriter(System.err);
        helpFormatter.printHelp(printWriter, helpFormatter.getWidth(), "java -jar tubular-cli.jar", null, options,
                helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(), null, true);
        printWriter.flush();
        printWriter.close();
    }
}
