import csv
import hashlib
import os

import pandas as pd
from tastypie import bundle

from dojo.models import Finding, Notes, Note_Type, User


class TempParser(object):
    # def __init__(self, filename, test):
    #     normalized_findings = self.normalize_findings(filename)
    #     self.ingest_findings(normalized_findings, test)

    def __init__(self, filename, test):
        dupes = dict()
        use_case_notes_dict = dict()
        vul_influence_notes_dict = dict()
        wso2_resolution_notes_dict = dict()
        resolution_notes_dict = dict()
        self.items = ()
        self.use_case_notes = ()
        self.vul_influence_notes = ()
        self.wso2_resolution_notes = ()
        self.resolution_notes = ()

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

            dupe_key = sev + str(cwe) + str(line) + str(sourcefile) + str(sourcefilepath) + str(title) + str(issue_id)

            if dupe_key in dupes:
                finding = dupes[dupe_key]
                if finding.description:
                    finding.description = finding.description + "\nVulnerability ID: " + \
                                          df.loc[i, 'mitigation']
                # self.process_endpoints(finding, df, i)
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

            use_case_note = Notes(entry=use_case_note, note_type=Note_Type(id=2), author=User.objects.all().first())
            vul_influence_note = Notes(entry=vul_influence_note, note_type=Note_Type(id=3), author=User.objects.all().first())
            wso2_resolution_note = Notes(entry=WSO2_resolution, note_type=Note_Type(id=1), author=User.objects.all().first())
            resolution_note = Notes(entry=resolution_note, note_type=Note_Type(id=4), author=User.objects.all().first())
            use_case_note.save()
            vul_influence_note.save()
            wso2_resolution_note.save()
            resolution_note.save()

            dupes[dupe_key] = finding
            use_case_notes_dict[dupe_key] = use_case_note
            vul_influence_notes_dict[dupe_key] = vul_influence_note
            wso2_resolution_notes_dict[dupe_key] = wso2_resolution_note
            resolution_notes_dict[dupe_key] = resolution_note

        self.items = list(dupes.values())
        self.use_case_notes = list(use_case_notes_dict.values())
        self.vul_influence_notes = list(vul_influence_notes_dict.values())
        self.wso2_resolution_notes = list(wso2_resolution_notes_dict.values())
        self.resolution_notes = list(resolution_notes_dict.values())
