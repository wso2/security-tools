#!/bin/bash

# Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


git clone https://github.com/DefectDojo/django-DefectDojo.git
cd django-DefectDojo

echo "Enter the git branch name that you want to checkout."
read branch

git checkout $branch

for file in ../diff-files/*
  do
    filename=$(basename "$file")
    echo "Apply the changes in $filename file. "
    git apply $file
done
