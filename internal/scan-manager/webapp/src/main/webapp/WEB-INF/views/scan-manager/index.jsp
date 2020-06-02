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
</head>
<body style="min-height: 0px;">
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="col-md-12">
            <h1 style="font-size: 55px;margin-top: 20px;text-align: center;">Security Scan Portal</h1>
        </div>
    </div>
    <hr>
    <div class="row">
        <div class="col-md-12" style="margin-top: 50px; text-align: center; margin-bottom: 20px;">
            <a class="btn btn-primary scan-manager-index-page-button" href="/scan-manager/scanners"
               role="button">Start a New Scan</a>
            <br>
            <a class="btn btn-primary scan-manager-index-page-button" href="/scan-manager/scans"
               role="button">View Scans</a>
            <br>
            <a class="btn btn-blue-grey scan-manager-index-page-button" href="/scan-manager/"
               role="button">Help</a>
            <br>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
