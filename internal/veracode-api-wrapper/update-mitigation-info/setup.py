#
# Copyright (c) 2019, WSO2 Inc., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

from setuptools import setup

setup(
    name='VeracodeApiWrapperUpdateMitigationInfo',
    version='1.0.0',
    packages=['update-mitigation-info'],
    url='https://github.com/wso2/security-tools',
    license='Apache License 2.0',
    author='WSO2 Platform Security Team',
    author_email='security@wso2.com',
    description='Veracode API wrapper to update mitigation info',
    install_requires=[
        'Python>=2.7.12', 'requests'
    ]
)
