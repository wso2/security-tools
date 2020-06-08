<%
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
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
    <script type="text/javascript" src="resources/custom/js/config.js"></script>
    <link href="resources/custom/css/tooltip.css" rel="stylesheet">
</head>
<body>
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scan Configuration</h1>
        </div>
    </div>
    <hr>
    <div class="row">
        <div class="col-md-6">
            <div class="row scan-manager-background-grey">
                <div class="col-md-12">
                    <form action="submit-scan" method="post"
                          enctype="multipart/form-data" onsubmit="loading()">
                        <div class="form-group required">
                            <div class="row">
                                <div class="col-md-11">
                                    <label for="scanName" style=" font-weight: 500">Scan Name</label>
                                </div>
                            </div>
                            <input type="text" class="form-control" id="scanName" name="scanName"
                                   placeholder="Please enter a scan name" required>
                        </div>
                        
                        <input type="hidden" class="form-control" id="scannerId"
                               name="scannerId" value="${scannerData.id}">
                        <input type="hidden" class="form-control" id="scannerName"
                               name="scannerName" value="${scannerData.name}">
                        <input type="hidden" class="form-control" id="scanType"
                               name="scanType" value="${scannerData.type}">
                        <c:if test="${productData.size() gt 0}">
                            <div class="form-group required">
                                <label for="productName" style="font-weight: 500">Product Name</label>
                                <select id="productName" name="productName" class="form-control form-control-lg">
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
                                            <div class="row">
                                                <div class="col-md-11">
                                                    <c:choose>
                                                        <c:when test="${field.isRequired()}">
                                                            <div class="required">
                                                                <label for="${field.fieldId}" style="font-weight:
                                            500">${field.displayName}</label>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <label for="${field.fieldId}" style="font-weight:
                                            500">${field.displayName}</label>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${not empty field.description}">
                                                    <div class="col-md-1 help-tip">
                                                        <p>${field.description}</p>
                                                    </div>
                                                </c:if>
                                            </div>
                                            <div id="group1">
                                                <div class="custom-control custom-radio">
                                                    <input type="radio"
                                                           onclick="javascript:
                                                                   changeFileUploadMethod('${field.fieldId}');"
                                                           value="file" class="custom-control-input"
                                                           id="${field.fieldId}byFileCheck"
                                                           name="${field.fieldId}FileUploadMethod">
                                                    <label class="custom-control-label"
                                                           for="${field.fieldId}byFileCheck">
                                                        By File</label>
                                                    <br>
                                                </div>
                                                <div class="custom-control custom-radio">
                                                    <input type="radio"
                                                           onclick="javascript:
                                                                   changeFileUploadMethod('${field.fieldId}');"
                                                           value="url" class="custom-control-input"
                                                           id="${field.fieldId}byURLCheck"
                                                           name="${field.fieldId}FileUploadMethod">
                                                    <label class="custom-control-label"
                                                           for="${field.fieldId}byURLCheck">By
                                                        URL</label>
                                                </div>
                                            </div>
                                            <c:choose>
                                                <c:when test="${field.isRequired()}">
                                                    <input type="file" name="${field.fieldId}" style="display:none;"
                                                           id="${field.fieldId}"
                                                           required>
                                                    <input type="text" class="form-control"
                                                           id="${field.fieldId}@byURL"
                                                           name="${field.fieldId}@byURL"
                                                           style="display:none;" required>
                                                </c:when>
                                                <c:otherwise>
                                                    <input type="file" name="${field.fieldId}" style="display:none;"
                                                           id="${field.fieldId}">
                                                    <input type="text" class="form-control"
                                                           id="${field.fieldId}@byURL"
                                                           name="${field.fieldId}@byURL"
                                                           style="display:none;">
                                                </c:otherwise>
                                            </c:choose>
                                            <div class="col-md-5" style="padding-left: 0px">
                                                <label id="${field.fieldId}FileSizeLabel"
                                                       style="display:none;">Max file size:${maxFileSize}</label>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:when test="${field.type eq 'checkbox'}">
                                        <div class="form-check custom-control custom-checkbox" style="margin-top:
                                        30px;">
                                            <div class="row">
                                                <div class="col-md-11">
                                                    <input type="checkbox" class="custom-control-input" value="true"
                                                           id="${field.fieldId}" name="${field.fieldId}">
                                                    <label class="custom-control-label" style="font-weight: 500"
                                                           for="${field.fieldId}">${field.displayName}</label>
                                                </div>
                                                <c:if test="${not empty field.description}">
                                                    <div class="col-md-1">
                                                        <div class="help-tip">
                                                            <p>${field.description}</p>
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="form-group">
                                            <c:choose>
                                                <c:when test="${field.isRequired()}">
                                                    <c:choose>
                                                        <c:when test="${field.type eq 'text'}">
                                                            <div class="required">
                                                                <div class="row">
                                                                    <div class="col-md-11">
                                                                        <label for="${field.fieldId}" style="font-weight:500">
                                                                                ${field.displayName}</label>
                                                                    </div>
                                                                    <c:if test="${not empty field.description}">
                                                                        <div class="col-md-1 help-tip">
                                                                            <p>${field.description}</p>
                                                                        </div>
                                                                    </c:if>
                                                                </div>
                                                                <input type="${field.type}" class="form-control"
                                                                       id="${field.fieldId}"
                                                                       name="${field.fieldId}" required>
                                                            </div>
                                                        </c:when>
                                                        <c:when test="${field.type eq 'dropdown'}">
                                                            <div class="required">
                                                                <div class="row">
                                                                    <div class="col-md-11">
                                                                        <label for="${field.fieldId}" style="font-weight:500">
                                                                                ${field.displayName}</label>
                                                                    </div>
                                                                    <c:if test="${not empty field.description}">
                                                                        <div class="col-md-1 help-tip">
                                                                            <p>${field.description}</p>
                                                                        </div>
                                                                    </c:if>
                                                                </div>
                                                                <select id="${field.fieldId}" name="${field.fieldId}" class="form-control form-control-lg">
                                                                    <c:forTokens items = "${field.propertyValueList}" delims = "," var = "propertyValue">
                                                                        <option value="${propertyValue}">${propertyValue}</option>
                                                                    </c:forTokens>
                                                                    </span>
                                                                </select>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="required">
                                                                <div class="row">
                                                                    <div class="col-md-11">
                                                                        <label for="${field.fieldId}" style="font-weight:500">
                                                                                ${field.displayName}</label>
                                                                    </div>
                                                                    <c:if test="${not empty field.description}">
                                                                        <div class="col-md-1 help-tip">
                                                                            <p>${field.description}</p>
                                                                        </div>
                                                                    </c:if>
                                                                </div>
                                                                <textarea name="${field.fieldId}" id="${field.fieldId}" class="form-control" rows="5" required></textarea>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:choose>
                                                        <c:when test="${field.type eq 'text'}">
                                                            <div class="row">
                                                                <div class="col-md-11">
                                                                    <label for="${field.fieldId}" style="font-weight: 500">
                                                                            ${field.displayName}</label>
                                                                </div>
                                                                <c:if test="${not empty field.description}">
                                                                    <div class="col-md-1 help-tip">
                                                                        <p>${field.description}</p>
                                                                    </div>
                                                                </c:if>
                                                            </div>
                                                            <input type="${field.type}" class="form-control"
                                                            id="${field.fieldId}"
                                                            name="${field.fieldId}">
                                                        </c:when>
                                                        <c:when test="${field.type eq 'dropdown'}">
                                                            <div class="row">
                                                                <div class="col-md-11">
                                                                    <label for="${field.fieldId}" style="font-weight: 500">
                                                                            ${field.displayName}</label>
                                                                </div>
                                                                <c:if test="${not empty field.description}">
                                                                    <div class="col-md-1 help-tip">
                                                                        <p>${field.description}</p>
                                                                    </div>
                                                                </c:if>
                                                            </div>
                                                            <select id="${field.fieldId}" name="${field.fieldId}" class="form-control form-control-lg">
                                                                <c:forTokens items = "${field.propertyValueList}" delims = "," var = "propertyValue">
                                                                    <option value="${propertyValue}">${propertyValue}</option>
                                                                </c:forTokens>
                                                                </span>
                                                            </select>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="row">
                                                                <div class="col-md-11">
                                                                    <label for="${field.fieldId}" style="font-weight: 500">
                                                                            ${field.displayName}</label>
                                                                </div>
                                                                <c:if test="${not empty field.description}">
                                                                    <div class="col-md-1 help-tip">
                                                                        <p>${field.description}</p>
                                                                    </div>
                                                                </c:if>
                                                            </div>
                                                            <textarea name="${field.fieldId}" id="${field.fieldId}" class="form-control" rows="5"></textarea>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:if>
                        <br>
                        <a class="btn btn-outline-primary" href="scanners" role="button">Back</a>
                        <button class="btn btn-primary">Submit Scan</button>
                        <div id="divLoading">
                        </div>
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
