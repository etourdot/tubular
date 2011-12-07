<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="2.0" xmlns:t="http://xproc.org/ns/testsuite" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.w3.org/2007/schema-for-xslt20.xsd" exclude-result-prefixes="t xs xsi xsl">

  <xsl:output method="text" />

  <xsl:variable name="document">
    <xsl:copy-of select="/" />
  </xsl:variable>

  <xsl:variable name="test-suite-uri" select="base-uri(/)" />

  <xsl:template match="/t:test-suite">

    <xsl:text>/*
 * Copyright (C) 2010 Herve Quiroz
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
package org.trancecode.xproc;

import java.net.URL;

import org.testng.annotations.Test;

/**
 * @author Herve Quiroz
 */
public class XProcTestSuiteTest extends AbstractXProcTestSuiteTest
{</xsl:text>

    <xsl:for-each select="('required', 'optional')">
      <xsl:variable name="category" select="." />
      <xsl:for-each select="$document/t:test-suite/t:test[starts-with(@href, $category)]">
        <xsl:sort select="@href" />
        <xsl:text>
    @Test
    public void </xsl:text>
        <xsl:value-of select="$category" />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="replace(replace(@href, '.*/(.*)\.xml', '$1'), '-', '_')" />
        <xsl:text>() throws Exception
    {
        test(new URL("</xsl:text>
        <xsl:value-of select="resolve-uri(@href, $test-suite-uri)" />
        <xsl:text>"), "</xsl:text>
        <xsl:value-of select="$category" />
        <xsl:text>");
    }
</xsl:text>
      </xsl:for-each>
    </xsl:for-each>
    <xsl:text>}&#xA;</xsl:text>

  </xsl:template>

</xsl:stylesheet>
