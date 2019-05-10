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
    <%@ page isELIgnored="false" %>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scanners</h1>
            <h4>Select a scanner to start scanning process </h4>
        </div>
    </div>
    <div class="col-md-6">
        <div class="row" style="margin-left: 0px; background-color: #a9a9a93b;
                                 border-radius: 10px; padding-right: 15px; padding-top: 20px; padding-bottom: 20px;">
            <div class="list-group">
                <form action="/scan-manager/configuration" method="post"
                      enctype="multipart/form-data">
                    <div class="col-md-12">
                        <c:if test="${staticScanners.size() > 0}">
                            <c:if test="${!staticScanners.get(0).name.equals('')
                        && !staticScanners.get(0).id.equals('')}">
                                <div class="row"
                                     style="margin-left: 0px; background-color: #fffdfd;
                                 border-radius: 10px; padding-right: 15px; padding-top: 20px; padding-bottom: 10px;">
                                    <div class="col-md-12" style="font-weight: 600; margin-left: 15px; padding-left: 0px;
                                 padding-right: 0px;">
                                        <h3 style="margin-top: 0px; margin-bottom: 15px;">Static</h3>
                                        <c:forEach begin="0" end="${staticScanners.size()-1}"
                                                   var="index">
                                            <input type="radio" name="scannerId" id="${staticScanners.get(index).id}"
                                                   value="${staticScanners.get(index).id}" checked>
                                            <label class="form-check-label" for="${staticScanners.get(index).id}">
                                                    ${staticScanners.get(index).name}</label>
                                            <br>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>
                        <br>
                        <c:if test="${dynamicScanners.size() > 0}">
                            <c:if test="${!dynamicScanners.get(0).name.equals('')
                        && !dynamicScanners.get(0).id.equals('')}">
                                <div class="row"
                                     style="margin-left: 0px; background-color: #fffdfd;
                                 border-radius: 10px; padding-right: 15px; padding-top: 20px; padding-bottom: 10px;">
                                    <div class="col-md-12" style="font-weight: 600; margin-left: 15px; padding-left: 0px;
                                 padding-right: 0px;">
                                        <h3 style="margin-top: 0px; margin-bottom: 15px;">Dynamic</h3>
                                        <c:forEach begin="0" end="${dynamicScanners.size()-1}"
                                                   var="index">
                                            <input type="radio" name="scannerId"
                                                   value="${dynamicScanners.get(index).id}" checked>
                                            <label class="form-check-label" for="${dynamicScanners.get(index).id}">
                                                    ${dynamicScanners.get(index).name}</label>
                                            <br>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>
                        <br>
                        <c:if test="${dependencyScanners.size() > 0}">
                            <c:if test="${!dependencyScanners.get(0).name.equals('')
                        && !dependencyScanners.get(0).id.equals('')}">
                                <div class="row"
                                     style="margin-left: 0px; background-color: #fffdfd;
                                 border-radius: 10px; padding-right: 15px; padding-top: 20px; padding-bottom: 10px;">
                                    <div class="col-md-12" style="font-weight: 600; margin-left: 15px; padding-left: 0px;
                                 padding-right: 0px;">
                                        <h3 style="margin-top: 0px; margin-bottom: 15px;">Dependency</h3>
                                        <c:forEach begin="0" end="${dependencyScanners.size()-1}" var="index">
                                            <input type="radio" name="scannerId"
                                                   value="${dependencyScanners.get(index).id}"
                                                   checked>
                                            <label class="form-check-label" for="${dependencyScanners.get(index).id}">
                                                    ${dependencyScanners.get(index).name}</label>
                                            <br>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>
                        <div class="row" style="margin-top: 25px; margin-left: 0px;">
                            <a class="btn btn-outline-primary" href="/scan-manager/" role="button">Back</a>
                            <button class="btn btn-primary">Next</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
