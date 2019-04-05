/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.configchecker.model;

import org.wso2.security.tools.configchecker.exception.ConfigCheckerException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates Configuration Node.
 */
public class ConfigNode {

    private String name = null;
    private String value = null;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<ConfigNode> children = new ArrayList<ConfigNode>();
    private ConfigNode parent = null;
    private Boolean isLeafNode = false;
    private Boolean hasAttributes = false;

    public ConfigNode(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) throws ConfigCheckerException {
        if (!children.isEmpty()) {
            throw new ConfigCheckerException("Cannot add a value as the node contains children");
        }
        this.isLeafNode = true;
        this.value = value;
    }

    public void addChild(ConfigNode child) throws ConfigCheckerException {
        if (!this.isLeafNode) {
            child.setParent(this);
            this.children.add(child);
        } else {
            throw new ConfigCheckerException("Cannot add a child into a leaf node");
        }
    }

    public List<ConfigNode> getChildren() {
        return children;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        if (!attributes.isEmpty()) {
            this.hasAttributes = true;
        }
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void setParent(ConfigNode parent) {
        this.parent = parent;
    }

    public ConfigNode getParent() {
        return parent;
    }

    public Boolean hasAttributes() {
        return hasAttributes;
    }

    public Boolean isLeafNode() {
        return isLeafNode;
    }
}
