/*
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

import java.util.ArrayList;

/**
 * Method - contains the method references of a given method inside a scanned class file.
 */
public class Method {

    private ArrayList<MethodReference> methodReferences;
    private String methodName;

    public Method(ArrayList<MethodReference> methodReferences, String methodName) {
        this.methodReferences = methodReferences;
        this.methodName = methodName;
    }

    public ArrayList<MethodReference> getMethodReferences() {
        return methodReferences;
    }

    public void setMethodReferences(ArrayList<MethodReference> methodReferences) {
        this.methodReferences = methodReferences;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
