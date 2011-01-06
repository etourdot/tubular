<?xml version="1.0"?>

<p:pipeline version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.w3.org/ns/xproc http://www.w3.org/TR/xproc/schemas/xproc.xsd">

  <p:identity name="identity1" />

  <p:store name="store1" href="file:/path/to/result" />

  <p:identity name="identity2">
    <p:input port="source">
      <p:document href="file:/path/to/source" />
    </p:input>
  </p:identity>

  <p:identity name="identity3" />

  <p:store name="store2" href="file:/path/to/result" />

  <p:identity name="identity4" />

  <p:load name="load1" href="file:/path/to/other/source" />

  <p:identity name="identity5" />

  <p:identity name="identity6">
    <p:input port="source">
      <p:pipe port="result" step="identity2" />
    </p:input>
  </p:identity>

</p:pipeline>
