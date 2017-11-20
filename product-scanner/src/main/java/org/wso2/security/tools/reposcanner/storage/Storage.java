/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.security.tools.reposcanner.storage;

import org.wso2.security.tools.reposcanner.entiry.Repo;
import org.wso2.security.tools.reposcanner.entiry.RepoArtifact;
import org.wso2.security.tools.reposcanner.entiry.RepoError;

public interface Storage {
    public boolean isRepoPresent(Repo repo) throws Exception;

    public boolean isArtifactPresent(Repo repo, String path) throws Exception;

    public boolean persist(RepoArtifact repoArtifactInfo) throws Exception;

    public boolean persistError(RepoError repoError) throws Exception;

    public void close();

    public boolean isErrorPresent(Repo repo, String path) throws Exception;
}
