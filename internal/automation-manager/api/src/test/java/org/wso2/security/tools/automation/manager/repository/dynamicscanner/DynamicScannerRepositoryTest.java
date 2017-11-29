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

package org.wso2.security.tools.automation.manager.repository.dynamicscanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.containerbased.zap.ZapEntity;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.DynamicScannerRepository;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DynamicScannerRepository}
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DynamicScannerRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DynamicScannerRepository dynamicScannerRepository;

    @Test
    public void testFindByUserId() throws Exception {
        String userId = "test@test.com";
        DynamicScannerEntity dynamicScannerToPersist = new ZapEntity();
        dynamicScannerToPersist.setUserId(userId);
        entityManager.persist(dynamicScannerToPersist);

        Iterable<DynamicScannerEntity> dynamicScanners = dynamicScannerRepository.findByUserId(userId);
        assertEquals(dynamicScannerToPersist, dynamicScanners.iterator().next());
    }
}
