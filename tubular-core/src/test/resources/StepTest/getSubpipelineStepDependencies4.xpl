<?xml version="1.0"?>

<p:pipeline version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.w3.org/ns/xproc http://www.w3.org/TR/xproc/schemas/xproc.xsd">

  <p:identity name="identity1" />

  <p:for-each name="for-each">
    <p:identity name="identity2" />
  </p:for-each>

</p:pipeline>
