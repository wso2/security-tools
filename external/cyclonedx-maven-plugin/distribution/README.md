# CycloneDx Maven Plugin 1.4.1-INTERNAL

This CycloneDx Maven Plugin 1.4.1-INTERNAL build jar has following change on top of cyclonedx-maven-plugin-1.4.1.

-  "getArtifacts()" method of maven core API was altered to "getDependencyArtifacts()" to identify only direct 
   dependencies of a project. Refer  <a href="https://maven.apache.org/ref/3.5.0/maven-core/apidocs/org/apache/maven/project/MavenProject.html#getDependencyArtifacts()">Maven Core API Documentation</a>

---

## How to install customized CycloneDx Maven Plugin 1.4.1-INTERNAL to local m2 repository. 

To get more information about BOM file and CycloneDX please refer < a href="https://cyclonedx.org/"> CycloneDx Documentation </a>

Step 1 : Download - <a href="https://github.com/wso2/security-tools/blob/master/external/cyclonedx-maven-plugin/distribution/cyclonedx-maven-plugin-1.4.1-INTERNAL.jar"> CycloneDx maven plugin </a>

Step 2 : Download - <a href="https://github.com/wso2/security-tools/blob/master/external/cyclonedx-maven-plugin/cyclonedx-maven-plugin-1.4.1/pom.xml"> pom file of CycloneDx maven plugin </a>

Step 3 : Run following command to install customized plugin into local m2 repository.

```
mvn install:install-file -Dfile=<Path of cyclonedx-maven-plugin-1.4.1-INTERNAL.jar> -DgroupId=org.cyclonedx -DartifactId=cyclonedx-maven-plugin -Dversion=1.4.1-INTERNAL -Dpackaging=maven-plugin -DpomFile=<Path of Pom File>
```