/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.security.tools.model;

/**
 * MethodReference - Holds details of the method invocations extracted from the class files.
 */
public class MethodReference {

    private String methodName;
    private String parentClass;
    private String usageMethod;
    private String usageClass;
    private int usageLineNumber;

    public MethodReference(String methodName, String parentClass, String usageMethod, String usageClass, int usageLineNumber) {
        this.methodName = methodName;
        this.parentClass = parentClass;
        this.usageMethod = usageMethod;
        this.usageClass = usageClass;
        this.usageLineNumber = usageLineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParentClass() {
        return parentClass;
    }

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public String getUsageMethod() {
        return usageMethod;
    }

    public void setUsageMethod(String usageMethod) {
        this.usageMethod = usageMethod;
    }

    public String getUsageClass() {
        return usageClass;
    }

    public void setUsageClass(String usageClass) {
        this.usageClass = usageClass;
    }

    public int getUsageLineNumber() {
        return usageLineNumber;
    }

    public void setUsageLineNumber(int usageLineNumber) {
        this.usageLineNumber = usageLineNumber;
    }
}
