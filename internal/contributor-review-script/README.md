# fork_monitor

Tool to monitor external collaborators of a GitHub repos in a GitHub fork tree

## Configuration
Create a `.env` file with following information
```bash
# GIT Token Info
TOKEN=''
```

## `fork_monitor` Help
```bash
usage: fork_monitor.py [-h] -o ORGANIZATION [--sleep SLEEP]

Simple Script to review contributors in forks

optional arguments:
  -h, --help            show this help message and exit
  -o ORGANIZATION, --organization ORGANIZATION
                        specify the Organization
  --sleep SLEEP         specify the delay between each request to the repo (default 3 seconds)
  ```
