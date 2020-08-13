/*
* Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import csv
import hashlib
import os

import pandas as pd
from tastypie import bundle

from dojo.models import Finding, Notes, Note_Type, User


class TempParser(object):

    def __init__(self, filename, test):
        dupes = dict()
        notes1 = dict()
        notes2 = dict()
        notes3 = dict()
        notes4 = dict()
        self.items = ()
        self.note1 = ()
        self.note2 = ()
        self.note5 = ()
        self.note6 = ()

        if filename is None:
            self.items = ()
            return

        df = pd.read_csv(filename, header=0)

        for i, row in df.iterrows():
            cwe = df.loc[i, 'cwe']
            title = df.loc[i, 'title']
            description = df.loc[i, 'description']
            sev = df.loc[i, 'severity']
            line = df.loc[i, 'line_number']
            issue_id = df.loc[i, 'issue_id']
            use_case_note = df.loc[i, 'Use_Case']
            vul_influence_note = df.loc[i, 'Vulnerability_Influence']
            resolution_note = df.loc[i, 'Resolution']
            sourcefilepath = df.loc[i, 'sourcefilepath']
            sourcefile = df.loc[i, 'sourcefile']
            mitigation = df.loc[i, 'mitigation']
            impact = df.loc[i, 'impact']
            WSO2_resolution = df.loc[i, 'WSO2_resolution']

            # dupe_key = hashlib.md5(str(cwe).encode('utf-8') + title.encode('utf-8')).hexdigest()
            # try:
            dupe_key = sev + str(cwe) + str(line) + str(sourcefile) + str(sourcefilepath) + str(title) + str(issue_id)
            # except:
            #     dupe_key = sev + flaw.attrib['cweid'] + flaw.attrib['module'] + flaw.attrib['type'] + flaw.attrib[
            #         'issueid']
            if dupe_key in dupes:
                finding = dupes[dupe_key]
                if finding.description:
                    finding.description = finding.description + "\nVulnerability ID: " + \
                                          df.loc[i, 'mitigation']
                dupes[dupe_key] = finding
            else:
                dupes[dupe_key] = True

                finding = Finding(title=title,
                                  cwe=int(cwe),
                                  test=test,
                                  active=False,
                                  verified=False,
                                  severity=sev,
                                  static_finding=True,
                                  line_number=line,
                                  file_path=sourcefilepath+sourcefile,
                                  line=line,
                                  sourcefile=sourcefile,
                                  description=description,
                                  numerical_severity=Finding.
                                  get_numerical_severity(sev),
                                  mitigation=mitigation,
                                  impact=impact,
                                  url='N/A')

            note3 = Notes(entry=use_case_note, note_type=Note_Type(id=2), author=User.objects.all().first())
            note4 = Notes(entry=vul_influence_note, note_type=Note_Type(id=3), author=User.objects.all().first())
            note6 = Notes(entry=WSO2_resolution, note_type=Note_Type(id=1), author=User.objects.all().first())
            note7 = Notes(entry=resolution_note, note_type=Note_Type(id=4), author=User.objects.all().first())
            note3.save()
            note4.save()
            note6.save()
            note7.save()

            dupes[dupe_key] = finding
            notes1[dupe_key] = note3
            notes2[dupe_key] = note4
            notes3[dupe_key] = note6
            notes4[dupe_key] = note7

            dupes[dupe_key] = finding
            notes1[dupe_key] = note3
            notes2[dupe_key] = note4
            notes3[dupe_key] = note6
            notes4[dupe_key] = note7

                # self.process_endpoints(finding, df, i)

        self.items = list(dupes.values())
        self.note1 = list(notes1.values())
        self.note2 = list(notes2.values())
        self.note5 = list(notes3.values())
        self.note6 = list(notes4.values())
        print(self.items)
