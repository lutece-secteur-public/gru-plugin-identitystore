![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=gru-plugin-identitystore-deploy)
# Plugin Identity Store

![](https://dev.lutece.paris.fr/plugins/plugin-identitystore/images/identitystore.png)

## Introduction

This plugin stores identities. An identity is composed of attributes. Each attribute can be read, written or certified by the application calling the service.

## REST API

The plugin provides a REST API to manage identities. Several versions of the API can be called:
 
* Version 1 : the URL to use is `/identity/rest/identitystore/v1/identity` . This version uses the objects of the packages `fr.paris.lutece.plugins.identitystore.v1.*` of the `library-identitystore` .
* Version 2 : the URL to use is `/identity/rest/identitystore/v2/identity` . This version uses the objects of the packages `fr.paris.lutece.plugins.identitystore.v2.*` of the `library-identitystore` .


The API can be called without version in the URL : `/identity/rest/identitystore/identity` . The used version is then the one configured with the key `identitystore.version` of the configuration file `identitystore.properties` . **Warning:** As the different versions of the API do not use the same objects, be sure the correct version is set in configuration.

## Encryption of identities

The plugin can use identities with encrypted ids. This mechanism permits to have a specific id for each service provider. Thus, the id is not shared.

To use encryption, the site has to contain a class implementing the interface `fr.paris.lutece.plugins.identitystore.service.encryption.IIdentityEncryptionService` from the library `gru-library-identitystore` . For example, add the plugin `gru-plugin-grukeydiversification` in the `pom.xml` of the site. The encryption is then enabled.

## Configuration

The bean names `identitystore.identityInfoExternalProvider` must be defined using Spring configuration. The bean must implement the Java interface `fr.paris.lutece.plugins.identitystore.service.external.IIdentityInfoExternalProvider` . It is used to create an identity from an external source when a connection id is provided in the web method *create* .

## Usage

The plugins exposes web methods to manage identities. Check */jsp/site/Portal.jsp?page=swaggerui* page for more details about these methods.

## Deletion of the identity

The deletion of an identity from the web service does not really delete it but flags it as 'deleted'. The attributes are deleted. Updating or retrieving an identity flagged 'deleted ' from the web service generates an exception.

Deleting an identity from the GUI completely deletes the identity from the BDD.


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-identitystore/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*