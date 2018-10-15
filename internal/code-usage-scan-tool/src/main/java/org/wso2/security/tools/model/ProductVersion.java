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

import java.util.ArrayList;

/**
 * ProductVersion - Version of the product which will contain the jars of the corresponding product version.
 */
public class ProductVersion {

    private int productVersion;
    private ArrayList<Jar> jars;

    public ProductVersion(int productVersion) {
        this.productVersion = productVersion;
        this.jars = new ArrayList<Jar>();
    }

    public int getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(int productVersion) {
        this.productVersion = productVersion;
    }

    public ArrayList<Jar> getJars() {
        return jars;
    }

    public void setJars(ArrayList<Jar> jars) {
        this.jars = jars;
    }
}
