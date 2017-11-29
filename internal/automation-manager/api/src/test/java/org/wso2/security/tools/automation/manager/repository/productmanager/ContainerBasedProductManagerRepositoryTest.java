/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.security.tools.automation.manager.repository.productmanager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.productmanager.containerbased
        .ContainerBasedProductManagerEntity;
import org.wso2.security.tools.automation.manager.repository.productmanager.ContainerBasedProductManagerRepository;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for
 * {@link org.wso2.security.tools.automation.manager.repository.productmanager.ContainerBasedProductManagerRepository}
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContainerBasedProductManagerRepositoryTest {

    private DynamicScannerEntity dynamicScannerEntityMock;
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContainerBasedProductManagerRepository productManagerRepository;

    @Before
    public void setup() {
        dynamicScannerEntityMock = Mockito.mock(DynamicScannerEntity.class);
    }

    @Test
    public void testFindOneByContainerId() throws Exception {
        String containerId = "testContainerId";
        String testName = "testName";
        String userId = "test@test.com";
        int relatedDynamicScannerId = 1;

        Mockito.when(dynamicScannerEntityMock.getId()).thenReturn(1);

        ContainerBasedProductManagerEntity productManagerToPersist = new ContainerBasedProductManagerEntity();
        productManagerToPersist.setContainerId(containerId);
        productManagerToPersist.setTestName(testName);
        productManagerToPersist.setUserId(userId);
        productManagerToPersist.setRelatedDynamicScannerId(relatedDynamicScannerId);
        entityManager.persist(productManagerToPersist);
        ContainerBasedProductManagerEntity productManager = productManagerRepository.findOneByContainerId(containerId);
        assertEquals(testName, productManager.getTestName());
    }

    @Test
    public void testFindByUserId() {
        String containerId = "testContainerId";
        String name = "testName";
        String userId = "test@test.com";
        int relatedDynamicScannerId = 1;
        Mockito.when(dynamicScannerEntityMock.getId()).thenReturn(1);

        ContainerBasedProductManagerEntity productManagerToPersist = new ContainerBasedProductManagerEntity();
        productManagerToPersist.setContainerId(containerId);
        productManagerToPersist.setTestName(name);
        productManagerToPersist.setUserId(userId);
        productManagerToPersist.setRelatedDynamicScannerId(relatedDynamicScannerId);
        entityManager.persist(productManagerToPersist);
        Iterable<ContainerBasedProductManagerEntity> productManagers = productManagerRepository.findByUserId(userId);
        assertEquals(productManagerToPersist.getUserId(), productManagers.iterator().next().getUserId());
    }
}
