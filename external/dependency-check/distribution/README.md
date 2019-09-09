# WSO2 Dependency Check 4.0.2

This Dependency Check distribution has following change on top of OWASP Dependency Check 4.0.2.

-  "parseDependencyVersion" method of OWASP Dependency Check Core was alter to check if the version contain "wso2" suffix. If so, version suffix was removed.

---

## How to use Dependency Check for WSO2 Products / Third party Dependency

Step 1 : Download - <a href="https://github.com/wso2/security-tools/blob/master/external/dependency-check/distribution/dependency-check.zip">Dependency-Check Distribution</a>

Step 2 : Unzip dependency-check.zip

Step 3 : Go to bin folder of extracted dependency-check distribution

Step 4 : Run following command as per requirement

```
./dependency-check.sh --project "<name of the project>" --scan <path to product pack or folder containing 3rd party libraries> --out <folder to generate reports> 	--format <Format of the report> --suppression <xml file containing suppressions>
```

For more information refer <a href="https://wso2.com/technical-reports/wso2-secure-engineering-guidelines#F">WSO2 secure Engineering Guidelines - External Dependency Analysis using OWASP Dependency Check</a>
