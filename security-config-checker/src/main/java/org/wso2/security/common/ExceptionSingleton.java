/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.security.common;

/**
 * This class is used check for occurrences of an Exception in the program.
 */
public class ExceptionSingleton {
    private static ExceptionSingleton instance;
    private  Boolean hasErrors = false;

    private ExceptionSingleton(){}

    public static synchronized  ExceptionSingleton getInstance() {
        if (instance == null) {
            instance = new ExceptionSingleton();
        }
        return instance;
    }

    public  Boolean getHasErrors() {
        return hasErrors;
    }

    public  void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}
