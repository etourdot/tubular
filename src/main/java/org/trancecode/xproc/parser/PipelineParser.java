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
package org.trancecode.xproc.parser;

import org.trancecode.xml.Location;
import org.trancecode.xml.SaxonLocation;
import org.trancecode.xml.SaxonUtil;
import org.trancecode.xproc.CompoundStep;
import org.trancecode.xproc.PipelineException;
import org.trancecode.xproc.PipelineFactory;
import org.trancecode.xproc.Port;
import org.trancecode.xproc.PortBinding;
import org.trancecode.xproc.PortReference;
import org.trancecode.xproc.Step;
import org.trancecode.xproc.Variable;
import org.trancecode.xproc.XProcPorts;
import org.trancecode.xproc.Port.Type;
import org.trancecode.xproc.binding.DocumentPortBinding;
import org.trancecode.xproc.binding.EmptyPortBinding;
import org.trancecode.xproc.binding.InlinePortBinding;
import org.trancecode.xproc.binding.PipePortBinding;
import org.trancecode.xproc.step.Pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;


/**
 * @author Herve Quiroz
 * @version $Revision$
 */
public class PipelineParser implements XProcXmlModel
{
	private final PipelineFactory pipelineFactory;
	private final Source source;
	private final Map<QName, StepFactory> importedLibrary;
	private final Map<QName, StepFactory> localLibrary = Maps.newHashMap();

	private final XLogger log = XLoggerFactory.getXLogger(getClass());

	private Pipeline pipeline;
	private Pipeline currentPipeline;
	private XdmNode rootNode;


	public PipelineParser(
		final PipelineFactory pipelineFactory, final Source source, final Map<QName, StepFactory> library)
	{
		assert pipelineFactory != null;
		this.pipelineFactory = pipelineFactory;

		assert source != null;
		this.source = source;

		assert library != null;
		this.importedLibrary = library;
	}


	public void parse()
	{
		log.entry();
		try
		{
			if (pipelineFactory.getUriResolver() != null)
			{
				pipelineFactory.getProcessor().getUnderlyingConfiguration().setURIResolver(
					pipelineFactory.getUriResolver());
			}

			final DocumentBuilder documentBuilder = pipelineFactory.getProcessor().newDocumentBuilder();
			documentBuilder.setLineNumbering(true);
			final XdmNode pipelineDocument = documentBuilder.build(source);
			rootNode = SaxonUtil.getElement(pipelineDocument, ELEMENTS_ROOT);
			if (rootNode.getNodeName().equals(ELEMENT_PIPELINE) || rootNode.getNodeName().equals(ELEMENT_DECLARE_STEP))
			{
				pipeline = parsePipeline(rootNode);
				parseInstanceStepBindings(rootNode, pipeline);
			}
			else if (rootNode.getNodeName().equals(ELEMENT_LIBRARY))
			{
				parseLibrary(rootNode);
			}
			else
			{
				unsupportedElement(rootNode);
			}
		}
		catch (final SaxonApiException e)
		{
			throw new PipelineException(e);
		}
	}


	private void parseLibrary(final XdmNode node)
	{
		for (final XdmNode childNode : SaxonUtil.getElements(node, ELEMENTS_DECLARE_STEP_OR_PIPELINE))
		{
			parsePipeline(childNode);
		}
	}


	private void parseInstanceStepBindings(final XdmNode node, final Step step)
	{
		for (final XdmNode withPortNode : SaxonUtil.getElements(node, ELEMENTS_PORTS))
		{
			parseWithPort(withPortNode, step);
		}

		for (final XdmNode variableNode : SaxonUtil.getElements(node, ELEMENT_VARIABLE))
		{
			parseVariable(variableNode, step);
		}

		for (final XdmNode withParamNode : SaxonUtil.getElements(node, ELEMENT_WITH_PARAM))
		{
			parseWithParam(withParamNode, step);
		}

		for (final XdmNode withOptionNode : SaxonUtil.getElements(node, ELEMENT_WITH_OPTION))
		{
			parseWithOption(withOptionNode, step);
		}

		// Syntactic shortcuts
		for (final Map.Entry<QName, String> attribute : SaxonUtil.getAttributes(node).entrySet())
		{
			final QName name = attribute.getKey();
			final String value = attribute.getValue();
			if (name.getNamespaceURI().isEmpty() && !name.equals(ATTRIBUTE_NAME) && !name.equals(ATTRIBUTE_TYPE))
			{
				log.trace("{} = {}", name, value);
				step.withOptionValue(name, value);
			}
		}
	}


	private void parseWithPort(final XdmNode portNode, final Step step)
	{
		final String name = getPortName(portNode);
		final Port port = step.getPort(name);
		assert port.isParameter() || port.getType().equals(getPortType(portNode)) : "port = " + port.getType()
			+ " ; with-port = " + getPortType(portNode);

		final Port configuredPort =
			port.setSelect(portNode.getAttributeValue(XProcXmlModel.ATTRIBUTE_SELECT)).setPortBindings(
				parsePortBindings(portNode));
		step.withPort(configuredPort);
	}


	private String getPortName(final XdmNode portNode)
	{
		if (ELEMENTS_STANDARD_PORTS.contains(portNode.getNodeName()))
		{
			return portNode.getAttributeValue(ATTRIBUTE_PORT);
		}

		if (portNode.getNodeName().equals(ELEMENT_ITERATION_SOURCE))
		{
			return XProcPorts.ITERATION_SOURCE;
		}

		if (portNode.getNodeName().equals(ELEMENT_XPATH_CONTEXT))
		{
			return XProcPorts.XPATH_CONTEXT;
		}

		throw new IllegalStateException(portNode.getNodeName().toString());
	}


	private Iterable<PortBinding> parsePortBindings(final XdmNode portNode)
	{
		return Iterables.transform(
			SaxonUtil.getElements(portNode, ELEMENTS_PORT_BINDINGS), new Function<XdmNode, PortBinding>()
			{
				@Override
				public PortBinding apply(final XdmNode node)
				{
					return parsePortBinding(node);
				}
			});
	}


	private void unsupportedElement(final XdmNode node)
	{
		throw new IllegalStateException(node.getNodeName().toString());
	}


	private static Location getLocation(final XdmNode node)
	{
		// TODO cache locations per node (slower at parsing but less memory used)
		return new SaxonLocation(node);
	}


	private StepFactory getStepFactory(final QName name)
	{
		final StepFactory localStepFactory = localLibrary.get(name);
		if (localStepFactory != null)
		{
			return localStepFactory;
		}

		final StepFactory importedStepFactory = importedLibrary.get(name);
		if (importedStepFactory != null)
		{
			return importedStepFactory;
		}

		throw new UnsupportedOperationException(name.toString());
	}


	private static <T> T getFirstNonNull(final T... values)
	{
		for (final T value : values)
		{
			if (value != null)
			{
				return value;
			}
		}

		throw new IllegalStateException(Arrays.asList(values).toString());
	}


	private void parsePort(final XdmNode portNode, final Step step)
	{
		final String portName = portNode.getAttributeValue(ATTRIBUTE_PORT);
		final Port.Type type = getPortType(portNode);

		final Port port =
			Port.newPort(step.getName(), portName, getLocation(portNode), type).setPrimary(
				portNode.getAttributeValue(ATTRIBUTE_PRIMARY)).setSequence(
				portNode.getAttributeValue(ATTRIBUTE_SEQUENCE)).setSelect(portNode.getAttributeValue(ATTRIBUTE_SELECT))
				.setPortBindings(parsePortBindings(portNode));
		log.trace("new port: {}", port);

		step.declarePort(port);
	}


	private static Type getPortType(final XdmNode node)
	{
		if (ELEMENTS_INPUT_PORTS.contains(node.getNodeName()))
		{
			return Type.INPUT;
		}

		if (node.getNodeName().equals(ELEMENT_INPUT))
		{
			if ("parameters".equals(node.getAttributeValue(ATTRIBUTE_KIND)))
			{
				return Type.PARAMETER;
			}
			else
			{
				return Type.INPUT;
			}
		}

		return Type.OUTPUT;
	}


	private PortBinding parsePortBinding(final XdmNode portBindingNode)
	{
		if (portBindingNode.getNodeName().equals(ELEMENT_PIPE))
		{
			final String stepName = portBindingNode.getAttributeValue(ATTRIBUTE_STEP);
			final String portName = portBindingNode.getAttributeValue(ATTRIBUTE_PORT);
			return new PipePortBinding(new PortReference(stepName, portName), getLocation(portBindingNode));
		}
		else if (portBindingNode.getNodeName().equals(ELEMENT_EMPTY))
		{
			return new EmptyPortBinding(getLocation(portBindingNode));
		}
		else if (portBindingNode.getNodeName().equals(ELEMENT_DOCUMENT))
		{
			final String href = portBindingNode.getAttributeValue(ATTRIBUTE_HREF);
			return new DocumentPortBinding(href, getLocation(portBindingNode));
		}
		else if (portBindingNode.getNodeName().equals(ELEMENT_INLINE))
		{
			final XdmNode inlineNode = SaxonUtil.getElement(portBindingNode);
			return new InlinePortBinding(inlineNode, getLocation(portBindingNode));
		}
		else
		{
			throw new PipelineException("not supported: {}", portBindingNode.getNodeName());
		}
	}


	private void parseOption(final XdmNode node, final Step step)
	{
		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
		final boolean required =
			getFirstNonNull(Boolean.parseBoolean(node.getAttributeValue(ATTRIBUTE_REQUIRED)), false);
		step.declareVariable(Variable.newOption(name, getLocation(node)).setSelect(select).setRequired(required));
	}


	private void parseWithParam(final XdmNode node, final Step step)
	{
		// TODO
		throw new UnsupportedOperationException();
	}


	private void parseWithOption(final XdmNode node, final Step step)
	{
		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
		step.withOption(name, select);
	}


	private void parseVariable(final XdmNode node, final Step step)
	{
		final QName name = new QName(node.getAttributeValue(ATTRIBUTE_NAME), node);
		final String select = node.getAttributeValue(ATTRIBUTE_SELECT);
		step.declareVariable(Variable.newVariable(name, getLocation(node)).setSelect(select).setRequired(true));
	}


	private Pipeline parsePipeline(final XdmNode node)
	{
		log.entry();

		final String name = getStepName(node);
		final QName type = SaxonUtil.getAttributeAsQName(node, ATTRIBUTE_TYPE);

		final Pipeline parsedPipeline = new Pipeline(name, getLocation(node), type);

		for (final XdmNode pipelineNode : SaxonUtil.getElements(node, ELEMENTS_DECLARE_STEP_OR_PIPELINE))
		{
			parsePipeline(pipelineNode);
		}

		currentPipeline = parsedPipeline;

		for (final XdmNode portNode : SaxonUtil.getElements(node, ELEMENTS_PORTS))
		{
			parsePort(portNode, parsedPipeline);
		}

		parsedPipeline.addImplicitPorts();

		for (final XdmNode optionNode : SaxonUtil.getElements(node, ELEMENT_OPTION))
		{
			parseOption(optionNode, parsedPipeline);
		}

		parseImports(node);

		parseSteps(node, parsedPipeline);

		currentPipeline = null;

		if (type != null)
		{
			localLibrary.put(type, parsedPipeline.getFactory());
		}

		return parsedPipeline;
	}


	private void parseImports(final XdmNode node)
	{
		for (final XdmNode importNode : SaxonUtil.getElements(node, ELEMENT_IMPORT))
		{
			parseImport(importNode);
		}
	}


	private void parseImport(final XdmNode node)
	{
		log.entry();
		final String href = node.getAttributeValue(ATTRIBUTE_HREF);
		assert href != null;
		log.trace("href = {}", href);
		final Source librarySource;
		try
		{
			librarySource = pipelineFactory.getUriResolver().resolve(href, source.getSystemId());
		}
		catch (final TransformerException e)
		{
			throw new PipelineException(e, "href = %s", href);
		}
		final PipelineParser parser = new PipelineParser(pipelineFactory, librarySource, pipelineFactory.getLibrary());
		parser.parse();
		final Map<QName, StepFactory> newLibrary = parser.getLibrary();
		log.trace("new steps = {}", newLibrary.keySet());
		localLibrary.putAll(newLibrary);
	}


	private Collection<QName> getSupportedStepTypes()
	{
		log.entry();
		// TODO improve performance
		final Collection<QName> types = new ArrayList<QName>();
		types.addAll(localLibrary.keySet());
		types.addAll(importedLibrary.keySet());
		log.trace("types = {}", types);
		return types;
	}


	private void parseSteps(final XdmNode node, final CompoundStep compoundStep)
	{
		for (final Step step : parseInnerSteps(node))
		{
			compoundStep.addStep(step);
		}
	}


	private List<Step> parseInnerSteps(final XdmNode node)
	{
		log.entry();

		return ImmutableList.copyOf(Iterables.transform(
			SaxonUtil.getElements(node, getSupportedStepTypes()), new Function<XdmNode, Step>()
			{
				@Override
				public Step apply(final XdmNode stepElement)
				{
					return parseInstanceStep(stepElement);
				}
			}));
	}


	private Step parseInstanceStep(final XdmNode node)
	{
		log.entry();
		final String name = getStepName(node);
		final QName type = node.getNodeName();
		log.trace("name = {} ; type = {}", name, type);

		final StepFactory stepFactory = getStepFactory(type);
		if (stepFactory != null)
		{
			final Step step = stepFactory.newStep(name, getLocation(node));
			parseInstanceStepBindings(node, step);
			log.trace("new instance step: {}", step);

			if (step instanceof CompoundStep)
			{
				parseSteps(node, (CompoundStep)step);
			}

			return step;
		}

		throw new UnsupportedOperationException("node = " + node.getNodeName() + " ; library = "
			+ getSupportedStepTypes());
	}


	private String getStepName(final XdmNode node)
	{
		final String explicitName = node.getAttributeValue(ATTRIBUTE_NAME);
		if (explicitName != null && explicitName.length() > 0)
		{
			return explicitName;
		}

		return getImplicitName(node);
	}


	private String getImplicitName(final XdmNode node)
	{
		return "!" + getImplicitName(rootNode, node);
	}


	private static String getImplicitName(final XdmNode rootNode, final XdmNode node)
	{
		if (rootNode == node || node.getParent() == null)
		{
			return "1";
		}

		final int index = getNodeIndex(node);
		final XdmNode parentNode = node.getParent();

		return getImplicitName(rootNode, parentNode) + "." + Integer.toString(index);
	}


	private static int getNodeIndex(final XdmNode node)
	{
		final XdmNode parentNode = node.getParent();
		if (parentNode == null)
		{
			return 1;
		}

		final List<XdmNode> childNodes = Lists.newArrayList(SaxonUtil.getElements(parentNode));
		assert childNodes.contains(node);

		return childNodes.indexOf(node);
	}


	public Pipeline getPipeline()
	{
		return pipeline;
	}


	public Map<QName, StepFactory> getLibrary()
	{
		return Collections.unmodifiableMap(localLibrary);
	}
}
