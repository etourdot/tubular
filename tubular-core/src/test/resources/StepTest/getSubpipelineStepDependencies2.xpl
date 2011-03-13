<?xml version="1.0" encoding="utf-8"?>

<p:pipeline name="pipeline" version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.w3.org/ns/xproc http://www.w3.org/TR/xproc/schemas/xproc.xsd">

  <p:wrap name="wrap" match="element" wrapper="wrapper" wrapper-namespace="http://baz.com" wrapper-prefix="baz" />

  <p:escape-markup name="escape-markup" />

  <p:choose name="choose">
    <p:when test="contains(/document, '&lt;baz:wrapper') and contains(/document, 'xmlns:baz=&quot;http://baz.com&quot;')">
      <p:identity>
        <p:input port="source">
          <p:inline>
            <success />
          </p:inline>
        </p:input>
      </p:identity>
    </p:when>
    <p:otherwise>
      <p:identity>
        <p:input port="source">
          <p:inline>
            <failure />
          </p:inline>
        </p:input>
      </p:identity>
    </p:otherwise>
  </p:choose>


</p:pipeline> 