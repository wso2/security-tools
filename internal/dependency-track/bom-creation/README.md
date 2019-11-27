# BOM File Creation for WSO2 products

Setup.sh and GenerateBOMFile.sh scripts are used to generate BOM (Bill of Material) file for a product pack or jar which is used to upload in Dependency Track.

## Overview of BOM file

A software bill of materials (software BOM) is a list of components in a piece of project. Software vendors often create products by assembling open source and commercial software components. The software BOM describes the components in a product. Main usage of software BOM is to perform vulnerability analysis. CycloneDX defines BOM namespace and it is used to generate BOM file. CycloneDX Java (Maven) Plugin provides the capability to generate the BOM file for maven projects.
For more information refer <a href="https://cyclonedx.org/"> CycloneDx Documentation </a>

## Why BOM File is required for Dependency Track?
So far Dependency Check xml report was used as an input resource for Dependency Track. But Dependency Track no longer supports Dependency Check. The best practice of Dependency Track is to use BOM file as an input resource.
For more information refer <a href="https://docs.dependencytrack.org/best-practices/"> Dependency Track Documentation </a>

## Setup environment to generate BOM file
If you are going to generate BOM file at first time, following prerequisites needs to be met. 

-CycloneDx maven plugin : By default CycloneDx supports transitive dependency.Therefore, we have done some modification in CycloneDx maven plugin and created a new internal version for our internal usage. This can be found in <a href="https://github.com/wso2/security-tools/blob/master/external/cyclonedx-maven-plugin/distribution/cyclonedx-maven-plugin-1.4.1-INTERNAL.jar"> CycloneDx maven plugin </a> 

-Setting.xml in local m2 repository : By default CycloneDx supports maven central. In order to support WSO2 custom repositories, we need to define those custom repositories in settings.xml in m2 local repository as well. 

To make developers life easier, Setup.sh script can be used to perform the above two tasks. This script will install the customized maven plugin in your m2 repository and create the settings.xml file with required repository configurations.

Execute following command to setup an environment using Setup.sh script.

```
./Setup.sh

```

## Generate BOM File
Inorder to populate the data in Dependency Track, we need to upload the BOM file of a product pack. GenerateBOMFile.sh script used to create a BOM file for a given product file. This script performs following subtask.

01. Unzip given product pack.
02. Unzip all war and jar files.
03. Create BOM file for each and every jar that can be found in a product pack.
04. Aggregate generated BOM files and generate master BOM file.

Execute following command to generate BOM file.


```
./GenerateBOMFile.sh  >> <Output log file name>.txt

```
