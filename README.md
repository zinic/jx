#Jx the JAXB JSON-XML Binding Model#

##The Project Structure##

This project has three maven POM files specified in it.

###net.jps:jx (/pom.xml)###

This is the parent POM file that controls things like dependency versions and
common project information bits and pieces.

###net.jps:jx-xjc (/jx-xjc/pom.xml)###

This is the XJC plugin portion of JX that allows users to use JX and XJC to
generate runtime persistent code anntations to better communicate binding
intent.

###net.jps:jx-xjc-test (/jx-xjc-test/pom.xml)###

This is a project dedicated to help test the generation rules outlined in the
XJC plugin.

##The Features##

* ```<jx:wrap />```

    Binds a wrap annotation to the XSD type, letting the JxJSON reader know that the JSON representation of the type will have an object wrapper ```{ ...wrapped contents... }``` placed around it.

* ```<jx:map as="[json-field-name]" />```

    Binds a JSON field name annotation to the XSD type or Element, allowing a user the ability to arbitrarily bind XSD names to JSON fields.


##The Examples##

###The XML###

[An annotated schema](https://github.com/zinic/jx/blob/master/jx-test/src/main/resources/META-INF/schema/limits.xsd) (because I'm lazy)

###The Javas###

[An example of how to write out JSON content from a JAXB object graph](https://github.com/zinic/jx/blob/master/jx-test/src/test/java/net/jps/jx/jackson/JxJsonWriterTest.java)
[An example of how to bind JSON content to a JAXB object graph](https://github.com/zinic/jx/blob/master/jx-test/src/test/java/net/jps/jx/jackson/JxJsonReaderTest.java)
