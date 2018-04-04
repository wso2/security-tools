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
package org.wso2.security.parsers.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.ConfigNode;
import org.wso2.security.common.ConfigurationBuilder;
import org.wso2.security.common.ExceptionSingleton;
import org.wso2.security.exceptions.IllegalOperationException;

/**
 * This class is used to parse JSON objects into ConfigNode objects.
 */
public class JSONConfigurationBuilder implements ConfigurationBuilder {
    private static final Logger log = LoggerFactory.getLogger(JSONConfigurationBuilder.class);
    private static final String  ROOT_NODE_NAME = "root";

    public ConfigNode parse(String   configurationString) {
        ConfigNode root = new ConfigNode(ROOT_NODE_NAME);

        try {
            JsonValue jsonValue = Json.parse(configurationString);
            setJSONConfigNodes(jsonValue.asObject(), root);
        } catch (Exception e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("Exception occurred when parsing json string", e);
        }
        return root;
    }

    /**
     * This method iteratively traverses the JSON tree and convert it into ConfigNode tree.
     * @param refJsonObject
     * @param parent
     */
    public void setJSONConfigNodes(JsonObject refJsonObject, ConfigNode parent) {

        try {
            for (JsonObject.Member member : refJsonObject) {

                JsonValue memberValue = member.getValue();
                if (memberValue.isString()) {
                    ConfigNode child = new ConfigNode(member.getName());
                    child.setValue(member.getValue().asString());
                    parent.addChild(child);
                }
                if (memberValue.isArray()) {
                    ConfigNode child = new ConfigNode(member.getName());
                    for (JsonValue jsonValue : memberValue.asArray()) {

                        if (jsonValue.isObject()) {
                            setJSONConfigNodes(jsonValue.asObject(), child);
                        }

                        if (jsonValue.isString()) {
                            child.setValue(jsonValue.asString());
                        }
                    }
                    parent.addChild(child);
                }
                if (memberValue.isObject()) {

                    ConfigNode child = new ConfigNode(member.getName());
                    setJSONConfigNodes(member.getValue().asObject(), child);
                    parent.addChild(child);
                }
            }
        } catch (IllegalOperationException e) {
            ExceptionSingleton.getInstance().setHasErrors(true);
            log.error("Error occurred while adding a child into " + parent.getName());
        }
    }
}
