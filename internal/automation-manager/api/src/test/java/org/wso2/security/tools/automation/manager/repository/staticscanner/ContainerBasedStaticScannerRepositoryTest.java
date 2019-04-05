/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.security.tools.automation.manager.repository.staticscanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.ContainerBasedStaticScannerEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.dependencycheck
        .DependencyCheckEntity;
import org.wso2.security.tools.automation.manager.repository.staticscanner.ContainerBasedStaticScannerRepository;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ContainerBasedStaticScannerRepository}
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContainerBasedStaticScannerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContainerBasedStaticScannerRepository staticScannerRepository;

    @Test
    public void testFindOneByContainerId() throws Exception {
        String containerId = "testContainerId";
        String testName = "testName";
        String userId = "test@test.com";

        ContainerBasedStaticScannerEntity staticScannerToPersist = new DependencyCheckEntity();
        staticScannerToPersist.setContainerId(containerId);
        staticScannerToPersist.setTestName(testName);
        staticScannerToPersist.setUserId(userId);

        entityManager.persist(staticScannerToPersist);
        ContainerBasedStaticScannerEntity staticScanner = staticScannerRepository.findOneByContainerId(containerId);
        assertEquals(testName, staticScanner.getTestName());
    }

    @Test
    public void testFindByUserId() {
        String containerId = "testContainerId";
        String testName = "testName";
        String userId = "test@test.com";

        ContainerBasedStaticScannerEntity staticScannerToPersist = new DependencyCheckEntity();
        staticScannerToPersist.setContainerId(containerId);
        staticScannerToPersist.setTestName(testName);
        staticScannerToPersist.setUserId(userId);
        entityManager.persist(staticScannerToPersist);

        Iterable<ContainerBasedStaticScannerEntity> staticScanners = staticScannerRepository.findByUserId(userId);
        assertEquals(staticScannerToPersist.getUserId(), staticScanners.iterator().next().getUserId());
    }
}
