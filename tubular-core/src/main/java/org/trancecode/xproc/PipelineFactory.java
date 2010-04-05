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
package org.trancecode.xproc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.parser.PipelineParser;
import org.trancecode.xproc.step.Choose;
import org.trancecode.xproc.step.CountStepProcessor;
import org.trancecode.xproc.step.ForEach;
import org.trancecode.xproc.step.IdentityStepProcessor;
import org.trancecode.xproc.step.LoadStepProcessor;
import org.trancecode.xproc.step.StoreStepProcessor;
import org.trancecode.xproc.step.When;
import org.trancecode.xproc.step.XsltStepProcessor;

/**
 * @author Herve Quiroz
 */
public class PipelineFactory
{
    private static final Logger LOG = Logger.getLogger(PipelineFactory.class);

    private static final String RESOURCE_PATH_XPROC_LIBRARY_1_0 = "/org/trancecode/xproc/xproc-1.0.xpl";

    protected static final StepProcessor UNSUPPORTED_STEP_PROCESSOR = new StepProcessor()
    {
        @Override
        public Environment run(final Step step, final Environment environment)
        {
            throw new UnsupportedOperationException(step.getType().toString());
        }
    };

    private static final Map<QName, Step> CORE_LIBRARY = getCoreLibrary();
    private static Map<QName, Step> defaultLibrary;
    private static Map<QName, StepProcessor> defaultProcessors;

    private Processor processor = new Processor(false);
    private URIResolver uriResolver = processor.getUnderlyingConfiguration().getURIResolver();
    private Map<QName, Step> library = getDefaultLibrary();

    public Pipeline newPipeline(final Source source)
    {
        LOG.trace("source = {}", source);

        assert source != null;

        if (processor == null)
        {
            processor = new Processor(false);
        }

        // TODO
        final PipelineParser parser = new PipelineParser(processor, source, getLibrary(), getStepProcessors());
        parser.parse();
        final Step pipeline = parser.getPipeline();
        if (pipeline == null)
        {
            throw new PipelineException("no pipeline could be parsed from %s", source.getSystemId());
        }

        // TODO pass the parsed pipeline to the runnable pipeline
        return new Pipeline(processor, uriResolver, pipeline);
    }

    public Map<QName, StepProcessor> getStepProcessors()
    {
        return Maps.newHashMap(getDefaultProcessors());
    }

    private static Map<QName, Step> getCoreLibrary()
    {
        final Map<QName, Step> coreSteps = Maps.newHashMap();

        coreSteps.put(XProcSteps.CHOOSE, Choose.STEP);
        coreSteps.put(XProcSteps.FOR_EACH, ForEach.STEP);
        coreSteps.put(XProcSteps.OTHERWISE, When.STEP_OTHERWISE);
        coreSteps.put(XProcSteps.WHEN, When.STEP_WHEN);
        // coreSteps.put(XProcSteps.GROUP, null);
        // coreSteps.put(XProcSteps.TRY, null);

        return ImmutableMap.copyOf(coreSteps);
    }

    private static Map<QName, Step> getDefaultLibrary()
    {
        if (defaultLibrary == null)
        {
            final Source defaultLibrarySource = new StreamSource(PipelineFactory.class
                    .getResourceAsStream(RESOURCE_PATH_XPROC_LIBRARY_1_0));
            final PipelineParser parser = new PipelineParser(new Processor(false), defaultLibrarySource, CORE_LIBRARY,
                    getDefaultProcessors());
            parser.parse();

            final Map<QName, Step> library = Maps.newHashMap();
            library.putAll(CORE_LIBRARY);
            library.putAll(parser.getLibrary());
            defaultLibrary = ImmutableMap.copyOf(library);
        }

        return defaultLibrary;
    }

    private static Map<QName, StepProcessor> getDefaultProcessors()
    {
        if (defaultProcessors == null)
        {
            final Map<QName, StepProcessor> processors = Maps.newHashMap();

            // Required steps
            // TODO
            processors.put(XProcSteps.COUNT, CountStepProcessor.INSTANCE);
            processors.put(XProcSteps.IDENTITY, IdentityStepProcessor.INSTANCE);
            processors.put(XProcSteps.LOAD, LoadStepProcessor.INSTANCE);
            processors.put(XProcSteps.STORE, StoreStepProcessor.INSTANCE);
            processors.put(XProcSteps.XSLT, XsltStepProcessor.INSTANCE);

            // Unsupported required steps
            processors.put(XProcSteps.ADD_ATTRIBUTE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.ADD_XML_BASE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.COMPARE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.DELETE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.DIRECTORY_LIST, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.ERROR, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.ESCAPE_MARKUP, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.FILTER, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.HTTP_REQUEST, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.INSERT, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.LABEL_ELEMENT, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.MAKE_ABSOLUTE_URIS, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.NAMESPACE_RENAME, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.PACK, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.PARAMETERS, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.RENAME, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.REPLACE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.SET_ATTRIBUTES, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.SINK, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.SPLIT_SEQUENCE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.STRING_REPLACE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.UNESCAPE_MARKUP, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.UNWRAP, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.WRAP, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.XINCLUDE, UNSUPPORTED_STEP_PROCESSOR);

            // Unsupported optional steps
            processors.put(XProcSteps.EXEC, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.HASH, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.UUID, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.VALIDATE_WITH_RELANXNG, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.VALIDATE_WITH_SCHEMATRON, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.VALIDATE_WITH_SCHEMA, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.WWW_FORM_URL_DECODE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.WWW_FORM_URL_ENCODE, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.XQUERY, UNSUPPORTED_STEP_PROCESSOR);
            processors.put(XProcSteps.XSL_FORMATTER, UNSUPPORTED_STEP_PROCESSOR);

            defaultProcessors = ImmutableMap.copyOf(processors);
        }

        return defaultProcessors;
    }

    public Map<QName, Step> getLibrary()
    {
        return library;
    }

    public void setLibrary(final Map<QName, Step> library)
    {
        assert library != null;
        this.library = library;
    }

    public void setUriResolver(final URIResolver uriResolver)
    {
        this.uriResolver = uriResolver;
    }

    public void setProcessor(final Processor processor)
    {
        assert processor != null;
        this.processor = processor;
    }

    public URIResolver getUriResolver()
    {
        return this.uriResolver;
    }

    public Processor getProcessor()
    {
        return this.processor;
    }
}
