#!/usr/bin/env python3

import os, sys
import json
import requests
import argparse
from time import sleep
from os.path import join, dirname
from dotenv import load_dotenv

dotenv_path = join(dirname(__file__), '.env')
load_dotenv(dotenv_path)        # take environment variables from .env.

class ForkMonitor:

        TOKEN = os.environ.get('TOKEN')

        def __init__(self) -> None:
                # get arguments
                args = self.get_args()
                self.org_name = args.organization
                self.sleep = args.sleep

                print("[!] Enumurating organization members..")
                self.org_members = self.get_members(self.org_name)

                print("[!] Enumurating organization repos..")
                self.org_repos = self.get_repos(self.org_name)

                self.fork_tree = dict()
                self.fin_output = list()


        def get_args(self) -> None:
                parser = argparse.ArgumentParser(description="Simple Script to review contributors in forks", formatter_class=argparse.RawTextHelpFormatter)
                parser.add_argument('-o', '--organization', type=str, help="specify the Organization", required=True)
                parser.add_argument('--sleep', type=int, help="specify the delay between each request to the repo (default 3 seconds)", default=3)

                return parser.parse_args()
        

        def tmp_print(self, response: json) -> None:
                print(json.dumps(response, indent=2, sort_keys=True))


        def get_data(self, url: str) -> list:
                """Making request to github APIs for given reletive URL"""
                
                headers = {
                        'User-Agent' : "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                        'Authorization' : f'token {self.TOKEN}'
                }

                return json.loads(requests.get(f'https://api.github.com/{url}', headers=headers).text)


        def get_repos(self, org_name: str) -> list:
                """Get repos of a given Organization"""
                org_repos = []
                page_number = 1
                while True:
                        # 100 repos per page
                        try:
                                page_repos = [repo['full_name'] for repo in self.get_data(f'orgs/{org_name}/repos?per_page=100&page={page_number}')]
                        except:
                                sys.exit("[!] Error occured ! Recheck the token, organization and try again.")
                        
                        if page_repos:
                                org_repos.extend(page_repos)
                                sleep(0.05)
                        else:
                                # no members in the page
                                break

                        page_number += 1
        
                return org_repos


        def get_members(self, org_name: str) -> list:   
                """Get members of a given Organization"""

                org_members = []
                page_number = 1
                while True:
                        # 100 members per page
                        try:
                                page_members = [collab['login'] for collab in self.get_data(f'orgs/{org_name}/members?per_page=100&page={page_number}')]
                        except:
                                sys.exit("[!] Error occured ! Recheck the token, organization and try again.")

                        if page_members:
                                org_members.extend(page_members)
                                sleep(0.05)
                        else:
                                # no members in the page
                                break

                        page_number += 1
        
                return org_members


        # ref : https://stackoverflow.com/questions/13687924/setting-a-value-in-a-nested-python-dictionary-given-a-list-of-indices-and-value
        def nested_set(self, dic, keys, value):
                for key in keys[:-1]:
                        dic = dic.setdefault(key, {})
                dic[keys[-1]] = value


        def build_forks_tree(self, dict_in: dict, path=[]) -> None:
                """Build forks tree from a given source dictonary"""

                for _, parent_repo in enumerate(dict_in):

                        forks_list = {}
                        try:
                                # get git forks if repo exist
                                forks_list = {repo['full_name']: dict() for repo in self.get_data(f'repos/{parent_repo}/forks')}
                                sleep(self.sleep)
                        except KeyboardInterrupt:
                                sys.exit("[!] Keyboard Interruption occured. Exiting !")
                        except:
                                print("[!] Skipping : directory not found")
                                continue

                        if forks_list:
                                # preparing to enumurate child branch
                                path.append(parent_repo)
                                # if forks exist
                                self.nested_set(self.fork_tree, path, forks_list)
                                yield from self.build_forks_tree(forks_list, path)
                                # go back to parent repo
                                path.pop()


        def get_collab(self, dict_in: dict, path=[]):
                """Get Collaborators of a given repo"""
 
                for repo in dict_in.keys():
                        path.append(repo)
                        # get assignees for each repo
                        try:
                                collab = [collab['login'] for collab in self.get_data(f"repos/{repo}/assignees")]
                        except:
                                print("[!] Error during user enumuration. Check if the token has correct privileges or user have access to the organization")
                        #  check for external users
                        diff_members = [member for member in collab if member not in self.org_members]

                        if diff_members:
                                yield repo, path, diff_members

                        yield from self.get_collab(dict_in[repo], path)
                        path.pop()


        def generate_final_out(self, repo: str, path: list, dif_collab: list) -> None:
                self.fin_output.append(
                        {
                                "repository" : repo,
                                "forked_chain" : list(path),
                                "external_users" : dif_collab
                        }
                )

        def main(self):

                for repo in self.org_repos:
                        self.fork_tree.update({f'{repo}' : dict()})

                # get org repos' forks
                print("[!] Generating fork tree. This may take few minutes....")
                for _ in self.build_forks_tree(self.fork_tree) : pass
                self.tmp_print(self.fork_tree)


                print("[!] Checking repos for external users...")
                for repo, path, diff_colab in self.get_collab(self.fork_tree):
                        self.generate_final_out(repo, path, diff_colab)

                if self.fin_output: 
                        self.tmp_print(self.fin_output)
                else:
                        sys.exit("[!] No external contributors were found")

if __name__ == "__main__":

        fm = ForkMonitor()
        fm.main()

