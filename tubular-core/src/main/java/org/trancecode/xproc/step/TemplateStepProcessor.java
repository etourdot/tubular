/*
 * Copyright (C) 2008 Herve Quiroz
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

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import org.trancecode.logging.Logger;
import org.trancecode.xml.saxon.Saxon;
import org.trancecode.xml.saxon.SaxonBuilder;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;

/**
 * {@code p:template}.
 * 
 * @author Lucas Soltic
 * @see <a href="http://www.w3.org/TR/xproc-template/#c.template">p:template</a>
 */
@ExternalResources(read = false, write = false)
public final class TemplateStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(TemplateStepProcessor.class);

    enum Mode {
        REGULAR, XPATH, SINGLE_QUOTE, DOUBLE_QUOTE
    }

    @Override
    public QName getStepType()
    {
        return XProcSteps.TEMPLATE;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        LOG.trace("p:template execution");

        final XdmNode templateNode = input.readNode(XProcPorts.TEMPLATE);
        final XdmNode sourceNode = input.readNode(XProcPorts.SOURCE);
        final SaxonBuilder builder = new SaxonBuilder(input.getPipelineContext().getProcessor()
                .getUnderlyingConfiguration());
        final Processor processor = input.getPipelineContext().getProcessor();
        builder.startDocument();
        processNode(templateNode, sourceNode, Axis.DESCENDANT_OR_SELF, input, builder, processor);
        builder.endDocument();
        LOG.trace("build result:{}", builder.getNode());
        output.writeNodes(XProcPorts.RESULT, builder.getNode());
        LOG.trace("end of p:template");
    }

    private XdmNode deepNodeCopy(final XdmNode sourceNode, final SaxonBuilder builder, final Processor processor)
    {
        final String stringRepresentation = sourceNode.toString();
        final XdmNode rebuiltNode = Saxon.parse(stringRepresentation, processor);
        return rebuiltNode;
    }

    private void processNode(final XdmNode templateNode, final XdmNode sourceNode, final Axis axis,
            final StepInput input, final SaxonBuilder builder, final Processor processor)
    {
        final XdmSequenceIterator iterator = templateNode.axisIterator(axis);

        while (iterator.hasNext())
        {
            final XdmItem currentItem = iterator.next();
            assert currentItem.isAtomicValue() == false;

            final XdmNode itemAsNode = (XdmNode) currentItem;
            final XdmNodeKind nodeKind = itemAsNode.getNodeKind();

            if (nodeKind == XdmNodeKind.ATTRIBUTE || nodeKind == XdmNodeKind.COMMENT
                    || nodeKind == XdmNodeKind.PROCESSING_INSTRUCTION || nodeKind == XdmNodeKind.TEXT)
            {
                final String nodeValue = itemAsNode.getStringValue();
                final QName nodeName = itemAsNode.getNodeName();

                LOG.trace("to be processed ({}): {}", nodeKind, itemAsNode);
                LOG.trace("string value: {}", nodeValue);

                final String evaluatedString = evaluateString(nodeValue, sourceNode, input, nodeKind);
                LOG.trace("evaluated string: '{}'", evaluatedString);

                switch (nodeKind)
                {
                    case ATTRIBUTE:
                        builder.attribute(nodeName, evaluatedString);
                        break;
                    case COMMENT:
                        builder.comment(evaluatedString);
                        break;
                    case PROCESSING_INSTRUCTION:
                        builder.processingInstruction(nodeName.toString(), evaluatedString);
                        break;
                    case TEXT:
                        if (evaluatedString.trim().length() > 0)
                        {
                            try
                            {
                                LOG.trace("text node to be inserted in tree:\n{}",
                                        Saxon.parse(evaluatedString, processor));
                                builder.nodes(Saxon.parse(evaluatedString, processor));
                            }
                            catch (final Exception e)
                            {
                                LOG.trace("text node to be inserted in tree:\n{}", evaluatedString);
                                builder.raw(evaluatedString);
                            }
                        }
                        else
                        {
                            LOG.trace("text node to be inserted in tree:\n{}", evaluatedString);
                            builder.text(evaluatedString);
                        }
                        break;
                    default:
                        LOG.error("unhandled node kind in switch");
                }

            }
            else
            {
                if (nodeKind == XdmNodeKind.ELEMENT)
                {
                    LOG.trace("to be processed as subtree ({}): {}", nodeKind, itemAsNode);
                    builder.startElement(itemAsNode.getNodeName());
                    processNode(itemAsNode, sourceNode, Axis.ATTRIBUTE, input, builder, processor);
                    // builder.endElement();
                }
                else
                {
                    LOG.error("unhandled node kind");
                }
            }
        }
    }

    private String evaluateString(final String stringToEvaluate, final XdmNode source, final StepInput input,
            final XdmNodeKind nodeKind)
    {
        Mode mode = Mode.REGULAR;
        String remainingCharacters = stringToEvaluate;
        String expression = "";
        String result = "";

        while (remainingCharacters.length() > 0)
        {
            final char firstCharacter = remainingCharacters.charAt(0);
            final String firstCharacterString = remainingCharacters.substring(0, 1);

            if (mode == Mode.REGULAR)
            {
                switch (firstCharacter)
                {
                    case '{':
                        if (remainingCharacters.length() > 1 && remainingCharacters.charAt(1) == '{')
                        {
                            result = result.concat("{");
                            remainingCharacters = remainingCharacters.substring(1);
                        }
                        else
                        {
                            expression = "";
                            mode = Mode.XPATH;
                        }
                        break;

                    case '}':
                        if (remainingCharacters.length() > 1 && remainingCharacters.charAt(1) == '}')
                        {
                            result = result.concat("}");
                            remainingCharacters = remainingCharacters.substring(1);
                        }
                        else
                        {
                            throw XProcExceptions.xc0067(input.getLocation());
                        }
                        break;

                    default:
                        result = result.concat(firstCharacterString);
                        break;
                }
            }
            else if (mode == Mode.XPATH)
            {
                switch (firstCharacter)
                {
                    case '{':
                        throw XProcExceptions.xc0067(input.getLocation());

                    case '}':
                        final XdmValue value = input.evaluateXPath(expression, source);

                        if (nodeKind == XdmNodeKind.TEXT)
                        {
                            result = result.concat(value.toString());
                        }
                        else
                        {
                            result = result.concat(value.itemAt(0).getStringValue());
                        }

                        LOG.trace("result of xpath eval:{} ({} items)", value.itemAt(0).getStringValue(), value.size());
                        LOG.trace("xpath res:{}", value);
                        LOG.trace("source tree:{}", source);
                        LOG.trace("resulting res:{}", result);
                        LOG.trace("remaining str:{}", remainingCharacters);
                        mode = Mode.REGULAR;
                        break;

                    case '\'':
                        expression = expression.concat("'");
                        mode = Mode.SINGLE_QUOTE;
                        break;

                    case '"':
                        expression = expression.concat("\"");
                        mode = Mode.DOUBLE_QUOTE;
                        break;

                    default:
                        expression = expression.concat(firstCharacterString);
                        break;
                }
            }
            else if (mode == Mode.SINGLE_QUOTE)
            {
                switch (firstCharacter)
                {
                    case '\'':
                        expression = expression.concat("'");
                        mode = Mode.XPATH;
                        break;

                    default:
                        expression = expression.concat(firstCharacterString);
                        break;
                }
            }
            else if (mode == Mode.DOUBLE_QUOTE)
            {
                switch (firstCharacter)
                {
                    case '\"':
                        expression = expression.concat("\"");
                        mode = Mode.XPATH;
                        break;

                    default:
                        expression = expression.concat(firstCharacterString);
                        break;
                }
            }

            remainingCharacters = remainingCharacters.substring(1);
        }

        if (mode != Mode.REGULAR)
        {
            throw XProcExceptions.xc0067(input.getLocation());
        }

        return result;
    }
}
