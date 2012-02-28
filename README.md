#JX the JAXB JSON-XML Binding Model#

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
(as I work on them)

* Allows users to annotate XSD entities with _<jx:wrap />_
