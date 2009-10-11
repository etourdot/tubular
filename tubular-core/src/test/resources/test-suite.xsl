<?xml version="1.0" encoding="UTF-8"?>

<!-- $Id$ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tc="http://www.trancecode.org" xmlns:saxon="http://saxon.sf.net/" xmlns:t="http://xproc.org/ns/testsuite"
  version="2.0">

  <xsl:output method="text"/>

  <xsl:template match="/t:test-suite">
    <xsl:for-each select="t:test/@href">
      <xsl:variable name="href" select="."/>
      <xsl:variable name="method-name" select="translate(substring-before(.,'.xml'), '-', '_')"/>
      <xsl:text>@Test public void </xsl:text>
      <xsl:value-of select="$method-name"/>
      <xsl:text>() throws Exception { test("</xsl:text>
      <xsl:value-of select="$href"/>
      <xsl:text>"); }&#xA;</xsl:text>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
