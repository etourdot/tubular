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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmNode;
import org.trancecode.logging.Logger;
import org.trancecode.xproc.XProcExceptions;
import org.trancecode.xproc.port.XProcPorts;
import org.trancecode.xproc.variable.XProcOptions;
import org.xml.sax.SAXException;

/**
 * Step processor for the p:validate-with-xml-schema standard XProc step.
 * 
 * @author Emmanuel Tourdot
 * @see <a
 *      href="http://www.w3.org/TR/xproc/#c.validate-with-xml-schema">p:validate-with-xml-schema</a>
 */
public final class ValidateWithSchemaStepProcessor extends AbstractStepProcessor
{
    private static final Logger LOG = Logger.getLogger(ValidateWithSchemaStepProcessor.class);

    @Override
    public QName getStepType()
    {
        return XProcSteps.VALIDATE_WITH_SCHEMA;
    }

    @Override
    protected void execute(final StepInput input, final StepOutput output)
    {
        final XdmNode sourceDoc = input.readNode(XProcPorts.SOURCE);
        final boolean useLocalHints = Boolean.parseBoolean(input.getOptionValue(XProcOptions.USE_LOCATION_HINTS,
                "false"));
        final boolean tryNamespaces = Boolean.parseBoolean(input.getOptionValue(XProcOptions.TRY_NAMESPACES, "false"));
        final boolean assertValid = Boolean.parseBoolean(input.getOptionValue(XProcOptions.ASSERT_VALID, "true"));
        final String mode = input.getOptionValue(XProcOptions.MODE, "strict");
        final Iterable<XdmNode> shemas = input.readNodes(XProcPorts.SCHEMA);
        boolean valid = false;
        try
        {
            final StringReader stringSource = new StringReader(getXmlDocument(sourceDoc));
            final StreamSource sourceSource = new StreamSource(stringSource);
            sourceSource.setSystemId(sourceDoc.getBaseURI().toASCIIString());
            for (final XdmNode schema : shemas)
            {
                final StringReader stringSchema = new StringReader(getXmlDocument(schema));
                final StreamSource sourceSchema = new StreamSource(stringSchema);
                sourceSchema.setSystemId(schema.getBaseURI().toASCIIString());
                valid = valid || validate(sourceSource, sourceSchema);
            }
        }
        catch (SaxonApiException e)
        {
        }
        if (assertValid && !valid)
        {
            throw XProcExceptions.xc0053(input.getLocation());
        }

        output.writeNodes(XProcPorts.RESULT, sourceDoc);
    }

    private boolean validate(final StreamSource sourceSource, final StreamSource schemaSource)
    {
        try
        {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = factory.newSchema(schemaSource);
            final Validator validator = schema.newValidator();
            validator.validate(sourceSource);
            return true;
        }
        catch (SAXException e)
        {
        }
        catch (IOException e)
        {
        }
        return false;
    }

    private static String getXmlDocument(final XdmNode node) throws SaxonApiException
    {
        final Serializer serializer = new Serializer();

        final XQueryCompiler xqcomp = node.getProcessor().newXQueryCompiler();
        final XQueryEvaluator xqeval = xqcomp.compile(".").load();
        xqeval.setContextItem(node);

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serializer.setOutputStream(stream);
        xqeval.setDestination(serializer);
        xqeval.run();

        try
        {
            return stream.toString("UTF-8");
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new IllegalStateException(uee);
        }
    }
}
