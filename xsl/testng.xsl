<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/2007/schema-for-xslt20.xsd"
  exclude-result-prefixes="xsi xsl">

  <xsl:output method="xml" indent="yes" />

  <xsl:param name="testng-results" />

  <xsl:template match="testsuite">
    <xsl:copy>
      <xsl:copy-of select="@* | properties" />
      <xsl:for-each select="testcase">
        <xsl:copy>
          <xsl:variable name="index" select="position()" />
          <xsl:copy-of select="@*[not(name() = 'name')]" />
          <xsl:attribute name="name">
            <xsl:choose>
              <xsl:when test="document($testng-results)//test-method[not (@is-config = 'true')][$index]/params">
                <xsl:value-of select="@name" />
                <xsl:text>(</xsl:text>
                <xsl:for-each
            select="document($testng-results)//test-method[not (@is-config = 'true')][$index]/params/param/value">
                  <xsl:if test="position() != 1">
                    <xsl:text>,</xsl:text>
                  </xsl:if>
                  <xsl:value-of select="replace(., '[ &#xA;]', '')" />
                </xsl:for-each>
                <xsl:text>)</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="@name" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:copy-of select="*" />
        </xsl:copy>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
