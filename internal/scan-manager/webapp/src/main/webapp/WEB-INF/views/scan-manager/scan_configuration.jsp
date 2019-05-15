<!--
~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scan Configuration</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="row"
                 style="margin-left: 0px;background-color: #a9a9a93b;border-radius: 10px;padding-right: 15px;">
                <div class="col-md-12">
                    <form action="/scan-manager/submit-scan" method="post"
                          enctype="multipart/form-data">
                        <div class="form-group">
                            <label for="scanName" style="margin-left: 15px; margin-top: 20px;">Scan Name</label>
                            <input type="text" class="form-control" id="scanName" name="scanName"
                                   placeholder="Please enter a scan name" style="margin-left: 15px;" required>
                        </div>
                        <input type="hidden" class="form-control" id="scannerId"
                               name="scannerId" value="${scannerData.id}">
                        <input type="hidden" class="form-control" id="scannerName"
                               name="scannerName" value="${scannerData.name}">
                        <input type="hidden" class="form-control" id="scanType"
                               name="scanType" value="${scannerData.type}">
                        
                        <c:if test="${productData.size() gt 0}">
                            <div class="form-group">
                                <label for="productName" style="margin-left: 15px; margin-top: 20px;">Product
                                    Name</label>
                                <select id="productName" name="productName" class="form-control form-control-lg"
                                        style="margin-left: 15px;">
                                    <c:forEach items="${productData}" var="product">
                                        <option value="${product}">${product}</option>
                                    </c:forEach>
                                    </span>
                                </select>
                            </div>
                        </c:if>
                        <c:if test="${scannerData.fields.size() gt 0}">
                            <c:forEach items="${scannerData.fields}" var="field">
                                <c:choose>
                                    <c:when test="${field.type eq 'file'}">
                                        <div class="form-group">
                                            <label for="${field.fieldId}" style="margin-left: 15px; margin-top:
                                20px;">${field.displayName}</label>
                                            <div id="group1">
                                                <input type="radio"
                                                       onclick="javascript:changeFileUploadMethod('${field.fieldId}');"
                                                       value="file"
                                                       id="${field.fieldId}byFileCheck"
                                                       name="${field.fieldId}FileUploadMethod"
                                                       style="margin-left: 15px;"> By File
                                                <br>
                                                <input type="radio"
                                                       onclick="javascript:changeFileUploadMethod('${field.fieldId}');"
                                                       value="url"
                                                       id="${field.fieldId}byURLCheck"
                                                       name="${field.fieldId}FileUploadMethod"
                                                       style="margin-left: 15px;"> By URL <br>
                                            </div>
                                            <input type="file" class="form-control" id="${field.fieldId}"
                                                   name="${field.fieldId}" style="display:none; margin-left: 15px;"
                                                   required>
                                            <input type="text" class="form-control"
                                                   id="${field.fieldId}@byURL"
                                                   name="${field.fieldId}@byURL"
                                                   style="display:none; margin-left: 15px;" required>
                                            
                                            <c:if test="${field.hasDefault}">
                                                <input type="checkbox" name="${field.fieldId}DefaultCheckbox"
                                                       style="margin-left:15px;" id="${field.fieldId}DefaultCheckbox"
                                                       value="true"
                                                       onclick="javascript:useDefault('${field.fieldId}');">
                                                <label class="form-check-label" for="${field.fieldId}DefaultCheckbox"
                                                       style="font-weight: 100;">use default</label>
                                            </c:if>
                                        </div>
                                    </c:when>
                                    <c:when test="${field.type == 'checkbox'}">
                                        <div class="form-check">
                                            <input type="checkbox" value="true" id="${field.fieldId}"
                                                   name="${field.fieldId}" style="margin-left: 15px;">
                                            <label class="form-check-label" for="${field.fieldId}"
                                                   style="margin-top: 20px; margin-bottom: 0px;">${field.displayName}</label>
                                            
                                            <c:if test="${field.hasDefault}">
                                                <br>
                                                <input type="checkbox" name="${field.fieldId}DefaultCheckbox"
                                                       style="margin-left:15px;" id="${field.fieldId}DefaultCheckbox"
                                                       value="true"
                                                       onclick="javascript:useDefault('${field.fieldId}');">
                                                <label class="form-check-label" for="${field.fieldId}DefaultCheckbox"
                                                       style="font-weight: 100;">use default</label>
                                            </c:if>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="form-group">
                                            <label for="${field.fieldId}" style="margin-left: 15px; margin-top:
                                20px;">${field.displayName}</label>
                                            <input type="${field.type}" class="form-control"
                                                   id="${field.fieldId}"
                                                   name="${field.fieldId}" style="margin-left: 15px;" required>
                                            <c:if test="${field.hasDefault}">
                                                <input type="checkbox" name="${field.fieldId}DefaultCheckbox"
                                                       style="margin-left:
                                                 15px;" id="${field.fieldId}DefaultCheckbox" value="true"
                                                       onclick="javascript:useDefault('${field.fieldId}');">
                                                <label class="form-check-label" for="${field.fieldId}DefaultCheckbox"
                                                       style="font-weight: 100;">use default</label>
                                            </c:if>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:if>
                        <br>
                        <a class="btn btn-outline-primary" href="/scan-manager/scanners" role="button">Back</a>
                        <button class="btn btn-primary">Submit Scan</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">

        /**
         * Files can be uploaded by uploading the file through the form or by providing a URL to download the
         * file. This function will display the appropriate input element (with the type 'file' or 'text') according to
         * the selected check box (by File or by URL).
         *
         * @param elementId element id of the file element that needs to be changed in to type 'file' or 'text'
         */
        function changeFileUploadMethod(elementId) {
            if (!document.getElementById(elementId + 'DefaultCheckbox').checked) {
                let fileElement = document.getElementById(elementId);   // input element with type file.
                let urlElement = document.getElementById(elementId + '@byURL'); // input element with type text.

                if (document.getElementById(elementId + 'byFileCheck') && document.getElementById(elementId +
                    'byFileCheck').checked) {

                    // if the user has selected the "by File" radio button.
                    // display the input element with the type file.
                    if (fileElement != null) {
                        fileElement.style.display = 'block';
                        fileElement.required = true;
                        fileElement.removeAttribute("disabled");
                    }

                    // hide the input element with the type text.
                    if (urlElement != null) {
                        urlElement.style.display = 'none';
                        urlElement.required = false;
                        urlElement.setAttribute("disabled", "disabled");
                    }
                } else if (document.getElementById(elementId + 'byURLCheck') && document.getElementById(elementId +
                    'byURLCheck').checked) {

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
                    }
                }
            }
        }
    </script>
    <script type="text/javascript">

        /**
         * This function is used to disable the element when 'use default' checkbox is checked.
         *
         * @param elementId element id of the element to be disabled
         */
        function useDefault(elementId) {

            // the element to be disabled.
            let currentElement = document.getElementById(elementId);

            // if the input element is a file upload by url.
            let currentFileURLElement = document.getElementById(elementId + '@byURL');

            // by File or by URL checkbox elements.
            let currentFileCheckbox = document.getElementById(elementId + 'byFileCheck');
            let currentURLCheckbox = document.getElementById(elementId + 'byURLCheck');

            if (document.getElementById(elementId + 'DefaultCheckbox').checked) {

                // if the "use default" is checked, disable input elements from submitting through the form.
                if (currentElement != null) {
                    currentElement.setAttribute("disabled", "disabled");
                }
                if (currentFileURLElement != null) {
                    currentFileURLElement.setAttribute("disabled", "disabled");
                }
                if (currentFileCheckbox != null) {
                    currentFileCheckbox.setAttribute("disabled", "disabled");
                }
                if (currentURLCheckbox != null) {
                    currentURLCheckbox.setAttribute("disabled", "disabled");
                }

            } else {

                // if the "use default" not checked, enable input elements from submitting through the form.
                if (currentElement != null) {
                    currentElement.removeAttribute("disabled");
                }
                if (currentFileURLElement != null) {
                    currentFileURLElement.removeAttribute("disabled");
                }
                if (currentFileCheckbox != null) {
                    currentFileCheckbox.removeAttribute("disabled");
                }
                if (currentURLCheckbox != null) {
                    currentURLCheckbox.removeAttribute("disabled");
                }
            }
        }
    </script>
    <%@ include file="../fragments/footer.jsp" %>
</body>
</html>
