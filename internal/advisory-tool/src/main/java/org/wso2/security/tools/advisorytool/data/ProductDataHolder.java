/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.security.tools.advisorytool.data;

import org.wso2.security.tools.advisorytool.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store the data from the Products.xml.
 */
public class ProductDataHolder {

    private static volatile ProductDataHolder instance;

    public static ProductDataHolder getInstance(){
        if(instance == null){
            synchronized (ProductDataHolder.class) {
                if(instance == null) {
                    instance = new ProductDataHolder();
                }
            }
        }
        return instance;
    }

    private ProductDataHolder(){}

    private List<Product> productList = new ArrayList<>();

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }






}
