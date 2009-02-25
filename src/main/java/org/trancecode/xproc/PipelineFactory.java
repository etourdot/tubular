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

import org.trancecode.log.Logger;
import org.trancecode.log.LoggerHelpers;
import org.trancecode.log.LoggerManager;
import org.trancecode.xml.Location;
import org.trancecode.xproc.parser.PipelineParser;
import org.trancecode.xproc.parser.StepFactory;
import org.trancecode.xproc.step.Choose;
import org.trancecode.xproc.step.Count;
import org.trancecode.xproc.step.ForEach;
import org.trancecode.xproc.step.Identity;
import org.trancecode.xproc.step.Load;
import org.trancecode.xproc.step.Otherwise;
import org.trancecode.xproc.step.Store;
import org.trancecode.xproc.step.When;
import org.trancecode.xproc.step.Xslt;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import com.google.common.collect.Maps;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class PipelineFactory implements LoggerHelpers
{
	private final Logger log = LoggerManager.getLogger(this);
	private Processor processor = new Processor(false);
	private URIResolver uriResolver = processor.getUnderlyingConfiguration().getURIResolver();
	private Map<QName, StepFactory> library = getDefaultLibrary();


	public Pipeline newPipeline(final Source source)
	{
		assert source != null;
		log.trace("%s pipelineSource = %s", METHOD_NAME, source);

		if (processor == null)
		{
			processor = new Processor(false);
		}

		// TODO
		final PipelineParser parser = new PipelineParser(this, source, getLibrary());
		parser.parse();
		final org.trancecode.xproc.step.Pipeline pipeline = parser.getPipeline();
		if (pipeline == null)
		{
			throw new PipelineException("no pipeline could be parsed from %s", source.getSystemId());
		}

		// TODO pass the parsed pipeline to the runnable pipeline
		return new Pipeline(processor, uriResolver, pipeline);
	}


	public Map<QName, StepFactory> newPipelineLibrary(final Source source)
	{
		final PipelineParser parser = new PipelineParser(this, source, getLibrary());
		parser.parse();
		// TODO
		return null;
	}


	private static StepFactory newUnsupportedStepFactory(final QName stepType)
	{
		return new StepFactory()
		{
			@Override
			public Step newStep(final String name, final Location location)
			{
				throw new UnsupportedOperationException("step type = " + stepType + " ; name = " + name
					+ " ; location = " + location);
			}
		};
	}


	private static void addUnsupportedStepFactory(final QName stepType, final Map<QName, StepFactory> library)
	{
		assert !library.containsKey(stepType);
		library.put(stepType, newUnsupportedStepFactory(stepType));
	}


	public static Map<QName, StepFactory> getDefaultLibrary()
	{
		final Map<QName, StepFactory> library = Maps.newHashMap();
		library.put(XProcSteps.CHOOSE, new Choose.Factory());
		library.put(XProcSteps.COUNT, new Count.Factory());
		library.put(XProcSteps.FOR_EACH, new ForEach.Factory());
		library.put(XProcSteps.IDENTITY, new Identity.Factory());
		library.put(XProcSteps.LOAD, new Load.Factory());
		library.put(XProcSteps.OTHERWISE, new Otherwise.Factory());
		library.put(XProcSteps.STORE, new Store.Factory());
		library.put(XProcSteps.WHEN, new When.Factory());
		library.put(XProcSteps.XSLT, new Xslt.Factory());

		// Unsupported core steps
		addUnsupportedStepFactory(XProcSteps.GROUP, library);
		addUnsupportedStepFactory(XProcSteps.TRY, library);

		// Unsupported required steps
		addUnsupportedStepFactory(XProcSteps.ADD_ATTRIBUTE, library);
		addUnsupportedStepFactory(XProcSteps.ADD_XML_BASE, library);
		addUnsupportedStepFactory(XProcSteps.COMPARE, library);
		addUnsupportedStepFactory(XProcSteps.DELETE, library);
		addUnsupportedStepFactory(XProcSteps.DIRECTORY_LIST, library);
		addUnsupportedStepFactory(XProcSteps.ERROR, library);
		addUnsupportedStepFactory(XProcSteps.ESCAPE_MARKUP, library);
		addUnsupportedStepFactory(XProcSteps.FILTER, library);
		addUnsupportedStepFactory(XProcSteps.HTTP_REQUEST, library);
		addUnsupportedStepFactory(XProcSteps.INSERT, library);
		addUnsupportedStepFactory(XProcSteps.LABEL_ELEMENT, library);
		addUnsupportedStepFactory(XProcSteps.MAKE_ABSOLUTE_URIS, library);
		addUnsupportedStepFactory(XProcSteps.NAMESPACE_RENAME, library);
		addUnsupportedStepFactory(XProcSteps.PACK, library);
		addUnsupportedStepFactory(XProcSteps.PARAMETERS, library);
		addUnsupportedStepFactory(XProcSteps.RENAME, library);
		addUnsupportedStepFactory(XProcSteps.REPLACE, library);
		addUnsupportedStepFactory(XProcSteps.SET_ATTRIBUTES, library);
		addUnsupportedStepFactory(XProcSteps.SINK, library);
		addUnsupportedStepFactory(XProcSteps.SPLIT_SEQUENCE, library);
		addUnsupportedStepFactory(XProcSteps.STRING_REPLACE, library);
		addUnsupportedStepFactory(XProcSteps.UNESCAPE_MARKUP, library);
		addUnsupportedStepFactory(XProcSteps.UNWRAP, library);
		addUnsupportedStepFactory(XProcSteps.WRAP, library);
		addUnsupportedStepFactory(XProcSteps.XINXLUDE, library);

		// Unsupported optional steps
		addUnsupportedStepFactory(XProcSteps.EXEC, library);
		addUnsupportedStepFactory(XProcSteps.HASH, library);
		addUnsupportedStepFactory(XProcSteps.UUID, library);
		addUnsupportedStepFactory(XProcSteps.VALIDATE_WITH_RELANXNG, library);
		addUnsupportedStepFactory(XProcSteps.VALIDATE_WITH_SCHEMATRON, library);
		addUnsupportedStepFactory(XProcSteps.VALIDATE_WITH_SCHEMA, library);
		addUnsupportedStepFactory(XProcSteps.WWW_FORM_URL_DECODE, library);
		addUnsupportedStepFactory(XProcSteps.WWW_FORM_URL_ENCODE, library);
		addUnsupportedStepFactory(XProcSteps.XQUERY, library);

		return library;
	}


	public Map<QName, StepFactory> getLibrary()
	{
		return library;
	}


	public void setLibrary(final Map<QName, StepFactory> library)
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
