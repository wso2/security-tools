/*
 *
 *  *  Copyright (c) ${YEAR} WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  WSO2 Inc. licenses this file to you under the Apache License,
 *  *  Version 2.0 (the "License"); you may not use this file except
 *  *  in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.security.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wso2.security.web.entity.Component;
import org.wso2.security.web.repository.ComponentRepository;

@Service
public class ComponentService {

    @Autowired
    private ComponentRepository componentRepository;

    public Object findAll() {
        return componentRepository.findAll();
    }

    public Component findById(Long id) {
        return componentRepository.findOne(id);
    }

    public Component save(Component component) {
        return componentRepository.save(component);
    }
}
