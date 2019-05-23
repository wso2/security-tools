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

/**
 * Files can be uploaded by uploading the file through the form or by providing a URL to download the
 * file. This function will display the appropriate input element (with the type 'file' or 'text') according to
 * the selected check box (by File or by URL).
 *
 * @param elementId element id of the file element that needs to be changed in to type 'file' or 'text'
 */
function changeFileUploadMethod(elementId) {
    let defaultCheckBox = document.getElementById(elementId + 'DefaultCheckbox');

    if (defaultCheckBox == null || !defaultCheckBox.checked) {
        let fileElement = document.getElementById(elementId);   // input element with type file.
        let urlElement = document.getElementById(elementId + '@byURL'); // input element with type text.
        let maxFileSizeLabel = document.getElementById(elementId + 'FileSizeLabel');

        if (document.getElementById(elementId + 'byFileCheck').checked) {

            // if the user has selected the "by File" radio button.
            // display the input element with the type file.
            if (fileElement != null) {
                fileElement.style.display = 'block';
                fileElement.required = true;
                fileElement.removeAttribute("disabled");

                maxFileSizeLabel.style.display = 'block';
                maxFileSizeLabel.removeAttribute("disabled");
            }

            // hide the input element with the type text.
            if (urlElement != null) {
                urlElement.style.display = 'none';
                urlElement.required = false;
                urlElement.setAttribute("disabled", "disabled");
            }
        } else if (document.getElementById(elementId + 'byURLCheck').checked) {

            // if the user has selected the "by URL" radio button.
            // display the input element with the type text.
            if (urlElement != null) {
                urlElement.style.display = 'block';
                urlElement.required = true;
                urlElement.removeAttribute("disabled");
            }

            // hide the input element with the type file.
            if (fileElement != null) {
                fileElement.setAttribute("disabled", "disabled");
                fileElement.style.display = 'none';
                fileElement.required = false;

                maxFileSizeLabel.style.display = 'none';
                maxFileSizeLabel.setAttribute("disabled", "disabled");
            }
        }
    }
}

/**
 * This function is used to display a waiting image during form submit.
 */
function loading() {
    let element = document.getElementById("divLoading");
    element.classList.add("show");
}