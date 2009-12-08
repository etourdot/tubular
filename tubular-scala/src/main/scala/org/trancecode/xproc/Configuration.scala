/*
 * Copyright 2009 TranceCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.trancecode.xproc

import javax.xml.transform.URIResolver
import net.sf.saxon.s9api._
import java.io.StringReader
import java.lang.IllegalStateException
import javax.xml.transform.stream.StreamSource

case class Configuration (baseUri: String, processor: Processor, uriResolver: URIResolver) {

  def readXmlDocument(uri: String): XdmNode = {
  // TODO
  null
  }

  def readXmlContent(xmlContent: String): XdmNode = {
    val reader = new StringReader(xmlContent)

    try {
      return processor.newDocumentBuilder().build(new StreamSource(reader))
    }
    catch {
      case e: SaxonApiException => throw new IllegalStateException(e)
    }
    finally
    {
      reader.close;
    }
  }

  def emptyDocument: XdmNode = readXmlContent("<?xml version=\"1.0\"?><document/>")

}
