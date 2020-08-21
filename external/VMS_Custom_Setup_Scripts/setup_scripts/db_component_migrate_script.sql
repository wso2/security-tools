/*
  -- Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  --
  -- WSO2 Inc. licenses this file to you under the Apache License,
  -- Version 2.0 (the "License"); you may not use this file except
  -- in compliance with the License.
  -- You may obtain a copy of the License at
  --
  --    http://www.apache.org/licenses/LICENSE-2.0
  --
  -- Unless required by applicable law or agreed to in writing,
  -- software distributed under the License is distributed on an
  -- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  -- KIND, either express or implied.  See the License for the
  -- specific language governing permissions and limitations
  -- under the License.
*/


CREATE PROCEDURE component_migrate()
    BEGIN
        DECLARE c int;
        SET c = 1 ;
        WHILE c<10 DO
            update dojo_finding p1 set p1.sourcefilepath=(select SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(p2.description,'Vulnerable Module:',2),'Vulnerable Module:',-1),'Type:',1) from(select * from dojo_finding) p2 where sourcefilepath IS NULL order by id limit 1),p1.description=(select SUBSTRING_INDEX(p2.description,'Vulnerable Module:',1)from (select * from dojo_finding) p2 where sourcefilepath IS NULL order by id limit 1) where p1.id=(select p2.id from(select * from dojo_finding) p2 where sourcefilepath IS NULL order by id limit 1);
            SET c = c + 1 ;
        END WHILE ;
    END