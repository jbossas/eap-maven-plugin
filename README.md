# eap-maven-plugin
Maven Plugin to provision EAP 8

# org.jboss.eap.plugins:eap-maven-plugin

  The eap-maven-plugin is used to provision an EAP server that contains your application.

## Goals Overview

  General information about the goals.

  * `eap:package` produces a fully configured EAP 8 server installation that contains your application. [package goal details](./doc/package.md) 

  * `eap:help` displays plugin help information. Call `mvn org.jboss.eap.plugins:eap-maven-plugin:help -Ddetail=true -Dgoal=package` to get the list of configuration parameters and associated description.

## Usage

This Maven plugin is to be used when producing EAP 8 application to be run on the Cloud. For example:

* To build a container image of an EAP application using the EAP 8 runtime image.
* To build an EAP application on Openshift using the EAP 8 S2I (Source To Image) builder image.
