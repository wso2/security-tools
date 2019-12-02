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
    <%@ page isELIgnored="false" %>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scanners</h1>
            <h5>Select a scanner to start scanning process </h5>
        </div>
    </div>
    <hr>
    <div class="row">
        <div class="col-md-6">
            <div class="row scan-manager-background-grey">
                <div class="col-md-12 list-view">
                    <form action="configuration" method="get"
                          enctype="multipart/form-data">
                        <div class="col-md-12">
                            <c:if test="${staticScanners.size() gt 0}">
                                <c:if test="${!staticScanners.get(0).name.equals('')
                        && !staticScanners.get(0).id.equals('')}">
                                    <div class="row scan-manager-background-white">
                                        <div class="col-md-12 scan-manager-column">
                                            <h3 class="scan-manager-scanner-type-label">Static</h3>
                                            <c:forEach begin="0" end="${staticScanners.size() - 1}"
                                                       var="index">
                                                <div class="col-md-10 custom-control custom-radio">
                                                    <input type="radio" class="custom-control-input" name="scannerId"
                                                           id="${staticScanners.get(index).id}"
                                                           value="${staticScanners.get(index).id}" checked>
                                                    <label class="custom-control-label" style="font-weight: 400;"
                                                           for="${staticScanners.get(index).id}">
                                                            ${staticScanners.get(index).name}</label>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                            </c:if>
                            <c:if test="${dynamicScanners.size() gt 0}">
                                <c:if test="${!dynamicScanners.get(0).name.equals('')
                        && !dynamicScanners.get(0).id.equals('')}">
                                    <div class="row scan-manager-background-white">
                                        <div class="col-md-12 scan-manager-column" >
                                            <h3 class="scan-manager-scanner-type-label">
                                                Dynamic</h3>
                                            <c:forEach begin="0" end="${dynamicScanners.size() - 1}"
                                                       var="index">
                                                <div class="col-md-10 custom-control custom-radio">
                                                    <input type="radio" class="custom-control-input" name="scannerId"
                                                           id="${dynamicScanners.get(index).id}"
                                                           value="${dynamicScanners.get(index).id}" checked>
                                                    <label class="custom-control-label" style="font-weight: 400;"
                                                           for="${dynamicScanners.get(index).id}">
                                                            ${dynamicScanners.get(index).name}</label>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                            </c:if>
                            <c:if test="${dependencyScanners.size() gt 0}">
                                <c:if test="${!dependencyScanners.get(0).name.equals('')
                        && !dependencyScanners.get(0).id.equals('')}">
                                    <div class="row scan-manager-background-white">
                                        <div class="col-md-12 scan-manager-column">
                                            <h3 class="scan-manager-scanner-type-label">
                                                Dependency</h3>
                                            <c:forEach begin="0" end="${dependencyScanners.size() - 1}" var="index">
                                                <div class="col-md-10 custom-control custom-radio">
                                                    <input type="radio" class="custom-control-input" name="scannerId"
                                                           id="${dependencyScanners.get(index).id}"
                                                           value="${dependencyScanners.get(index).id}"
                                                           checked>
                                                    <label class="custom-control-label" style="font-weight: 400;"
                                                           for="${dependencyScanners.get(index).id}">
                                                            ${dependencyScanners.get(index).name}</label>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>
                            </c:if>
                            <div class="row" style="margin-top: 15px; margin-left: 0px;">
                                <a class="btn btn-outline-primary" href="/" role="button">Back</a>
                                <button class="btn btn-primary">Next</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
