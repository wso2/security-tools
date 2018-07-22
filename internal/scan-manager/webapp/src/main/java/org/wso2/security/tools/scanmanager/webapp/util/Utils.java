/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.scanmanager.webapp.util;

import org.wso2.security.tools.scanmanager.webapp.model.Scanner;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private Utils() {
    }

    /**
     * Filter the scanners by given scanner type.
     *
     * @param scannerList list of scanner objects
     * @param type        scanner type
     * @return a list of scanner objects with the given type
     */
    public static List<Scanner> getScannersByType(List<Scanner> scannerList, String type) {
        List<Scanner> filteredScannerList = new ArrayList<>();

        for (Scanner scanner : scannerList) {
            if (scanner.getType().equals(type)) {
                filteredScannerList.add(scanner);
            }
        }
        return filteredScannerList;
    }
}
