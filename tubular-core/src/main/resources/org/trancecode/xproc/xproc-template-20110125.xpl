<?xml version="1.0" encoding="UTF-8"?>
<!-- http://www.w3.org/TR/2011/NOTE-xproc-template-20110125/ -->
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0">
  <p:declare-step type="p:in-scope-names">
    <p:output port="result" primary="false" />
  </p:declare-step>
  <p:declare-step type="p:template">
    <p:input port="template" />
    <p:input port="source" sequence="true" primary="true" />
    <p:input port="parameters" kind="parameter" />
    <p:output port="result" />
  </p:declare-step>
</p:library>
