#Jx the JAXB JSON-XML Binding Model and XJC Plugin#

###What is it?###

Jx is an annotation framework that's inspired by [JXON](http://www.balisage.net/Proceedings/vol7/html/Lee01/BalisageVol7-Lee01.html).

Jx cuts out a lot of JXON's concerns by relying on the well established transformations and bindings that exist in [JAXB](http://www.oracle.com/technetwork/articles/javase/index-140168.html).

###Cool... so what's it do?###

Jx allows you to annotate an XML schema document using JAXB binding customizations. These annotations are used to help direct the processing and generation of JSON structural renderings derived from the XSD in a predictable, type-safe manner.

Jx makes it trivial to support XML and JSON with ONE schema contract (the XSD). Jx will automatically map both XML and JSON representations of the schema to the underlying Java JAXB classes.


##The Features##

###Code Features###

* Streaming JAXB to JSON generator

* Streaming JSON to JAXB mapper

###Jx XSD Annotations###

* ```<jx:wrap />```

    Binds a wrap annotation to the XSD type, letting the JxJSON reader know that the JSON representation of the type will have an object wrapper ```{ ...wrapped contents... }``` placed around it.

* ```<jx:map as="[json-field-name]" />```

    Binds a JSON field name annotation to the XSD type or Element, allowing a user the ability to arbitrarily bind XSD names to JSON fields.


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


##The Examples##

###The XML###

[A Jx annotated example schema](https://github.com/zinic/jx/blob/master/jx-test/src/main/resources/META-INF/schema/limits.xsd)

###The Javas###

[An example of how to write out JSON content from a JAXB object graph](https://github.com/zinic/jx/blob/master/jx-test/src/test/java/net/jps/jx/jackson/JxJsonWriterTest.java)

[An example of how to bind JSON content to a JAXB object graph](https://github.com/zinic/jx/blob/master/jx-test/src/test/java/net/jps/jx/jackson/JxJsonReaderTest.java)


##That Legal Thing...##

This software library is realease to you under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). See [LICENSE](https://github.com/zinic/jx/blob/master/LICENSE) for more information.
