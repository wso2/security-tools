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
            <h1>Scans</h1>
        </div>
        <c:choose>
            <c:when test="${scanList.scans.size() > 0}">
                <ul class="list-group">
                    <div class="col-lg-6 col-md-12 col-sm-12">
                        <div class="row">
                            <c:forEach begin="0" end="${scanList.scans.size()-1}" var="index">
                                <div class="col-lg-8 col-md-6 col-sm-9">
                                    <li class="list-group-item">
                                            ${scanList.scans.get(index).name}
                                        <span class="badge">${scanList.scans.get(index).status}</span>
                                    </li>
                                </div>
                                <div class="col-lg-4 col-md-6 col-sm-3">
                                    <form action="/scanManager/stop" method="post"
                                          enctype="multipart/form-data">
                                        <c:if test="${scanList.scans.get(index).status.equals('Running')}">
                                            <span>  <button
                                                    class="btn btn-danger">Stop</button></span>
                                        </c:if>
                                        <input type="hidden" name="id"
                                               value="${scanList.scans.get(index).id}">
                                    </form>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </ul>
            </c:when>
            <c:otherwise>
                <div>
                    <h4>No Scans found</h4>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
