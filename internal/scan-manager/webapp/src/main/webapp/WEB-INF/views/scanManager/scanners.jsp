<!--
~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
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
<%@ include file="../fragments/navBar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scanners</h1>
            <h4>Select a scanner to start scanning process </h4>
        </div>
        <div class="list-group">
            <form action="/scanManager/scanConfiguration" method="post"
                  enctype="multipart/form-data">
                <div class="col-lg-6 col-md-12 col-sm-8">

                    <c:if test="${scannerConfig.staticScanners.size() > 0}">
                        <c:if test="${!scannerConfig.staticScanners.get(0).name.equals('')
                        && !scannerConfig.staticScanners.get(0).id.equals('')}">
                            <div class="jumbotron"
                                 style="padding-top: 10px; padding-bottom: 15px; padding-left: 20px">
                                <p>Static</p>
                                <c:forEach begin="0" end="${scannerConfig.staticScanners.size()-1}"
                                           var="index">
                                    <input type="radio" name="scannerID"
                                           value="${scannerConfig.staticScanners.get(index).id}" checked>
                                    <c:out
                                            value="${scannerConfig.staticScanners.get(index).name}"></c:out>
                                    <br>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:if>
                    <br>
                    <c:if test="${scannerConfig.dynamicScanners.size() > 0}">
                        <c:if test="${!scannerConfig.dynamicScanners.get(0).name.equals('')
                        && !scannerConfig.dynamicScanners.get(0).id.equals('')}">
                            <div class="jumbotron"
                                 style="padding-top: 10px; padding-bottom: 15px; padding-left: 20px">
                                <p>Dynamic</p>

                                <c:forEach begin="0" end="${scannerConfig.dynamicScanners.size()-1}"
                                           var="index">
                                    <input type="radio" name="scannerID"
                                           value="${scannerConfig.dynamicScanners.get(index).id}" checked>
                                    <c:out
                                            value="${scannerConfig.dynamicScanners.get(index).name}"></c:out>
                                    <br>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:if>
                    <br>
                    <c:if test="${scannerConfig.dependencyScanners.size() > 0}">
                        <c:if test="${!scannerConfig.dependencyScanners.get(0).name.equals('')
                        && !scannerConfig.dependencyScanners.get(0).id.equals('')}">
                            <div class="jumbotron"
                                 style="padding-top: 10px; padding-bottom: 15px; padding-left: 20px">
                                <p>Dependency</p>
                                <c:forEach begin="0"
                                           end="${scannerConfig.dependencyScanners.size()-1}"
                                           var="index">
                                    <input type="radio" name="scannerID"
                                           value="${scannerConfig.dependencyScanners.get(index).id}"
                                           checked> <c:out
                                        value="${scannerConfig.dependencyScanners.get(index).name}"></c:out>
                                    <br>
                                </c:forEach>
                            </div>
                        </c:if>
                    </c:if>
                    <a class="btn btn-outline-primary" href="/scanManager/" role="button">Back</a>
                    <button class="btn btn-primary">Next</button>

                </div>
            </form>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
