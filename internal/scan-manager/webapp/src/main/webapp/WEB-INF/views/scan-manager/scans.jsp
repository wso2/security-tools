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
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@ include file="../fragments/nav_bar.jsp" %>
<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div class="row">
                <div class="page-header col-md-12" style="padding-left: 0px;">
                    <h1>Scans</h1>
                    <h5>To view the scans under preparation
                        <a href="${window.location.href}#preparing-scans">click here</a>
                    </h5>
                </div>
            </div>
            <hr>
            <c:choose>
                <c:when test="${scanListResponse.scanList.size() gt 0}">
                    <c:forEach begin="0" end="${scanListResponse.scanList.size() - 1}" var="index">
                        <div class="row scan-manager-background-grey">
                            <div class="col-md-8" style="margin-bottom: 15px;">
                                <b>Name: </b>${e:forHtml(scanListResponse.scanList.get(index).name)}</br>
                                <b>Job ID: </b>${scanListResponse.scanList.get(index).jobId}</br>
                                <c:choose>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                             .equals('SUBMIT_PENDING') ||
                                             scanListResponse.scanList.get(index).status.name()
                                             .equals('SUBMITTED')}">
                                        <b>Status: </b>Queued</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('RUNNING')}">
                                        <b>Status: </b>Running</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('ERROR')}">
                                        <b>Status: </b>Error</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('COMPLETED')}">
                                        <b>Status: </b>Complete</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('CANCEL_PENDING')}">
                                        <b>Status: </b>Cancel Pending</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('CANCELED')}">
                                        <b>Status: </b>Canceled</br>
                                    </c:when>
                                    <c:when test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('PREPARING')}">
                                        <b>Status: </b>Preparing</br>
                                    </c:when>
                                    <c:otherwise>
                                        <b>Status: </b>Unknown</br>
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${not empty scanListResponse.scanList.get(index).priority}">
                                        <b>Priority: </b>${scanListResponse.scanList.get(index).priority}</br>
                                    </c:when>
                                    <c:otherwise>
                                        <b>Priority: </b>Undefined</br>
                                    </c:otherwise>
                                </c:choose>
                                <b>Scanner: </b>${scanListResponse.scanList.get(index).scannerName}</br>
                                <b>Product: </b>${scanListResponse.scanList.get(index).product}</br>
                                <b>Created Time: </b>${scanListResponse.scanList.get(index).submittedTimestamp}
                            </div>
                            <div class="col-md-4">
                                <c:if test="${scanListResponse.scanList.get(index)
                                                .status.name().equals('COMPLETED')}">
                                    <form action="report" method="get"
                                          class="scan-manager-scan-action-button">
                                        <input type="hidden" name="jobId"
                                               value="${scanListResponse.scanList.get(index).jobId}"/>
                                        <button class="btn btn-primary">Scan Report</button>
                                    </form>
                                </c:if>
                                <c:if test="${scanListResponse.scanList.get(index).status.name()
                                                .equals('RUNNING') ||
                                             scanListResponse.scanList.get(index).status.name()
                                             .equals('SUBMIT_PENDING') ||
                                             scanListResponse.scanList.get(index).status.name()
                                             .equals('SUBMITTED')}">
                                    <form action="stop" method="post"
                                          class="scan-manager-scan-action-button">
                                        <input type="hidden" name="jobId"
                                               value="${scanListResponse.scanList.get(index).jobId}"/>
                                        <button class="btn btn-danger">Cancel</button>
                                    </form>
                                </c:if>
                                <form action="logs" method="get"
                                      class="scan-manager-scan-action-button">
                                    <input type="hidden" name="jobId"
                                           value="${scanListResponse.scanList.get(index).jobId}"/>
                                    <button class="btn btn-warning">Logs</button>
                                </form>
                                </span>
                            </div>
                        </div>
                        <br>
                    </c:forEach>
                    <div class="row">
                        <div class="col-md-6" style="padding-left: 0px;">
                            <form action="scans" method="get" class="scan-manager-page-nav-button">
                                <input type="hidden" name="page" value="1"/>
                                <c:choose>
                                    <c:when test="${!scanListResponse.isFirstPage()}">
                                        <button class="btn btn-primary">First</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-primary" disabled>First</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                            <form action="scans" method="get" class="scan-manager-page-nav-button">
                                <input type="hidden" name="page" value="${scanListResponse.currentPage - 1}"/>
                                <c:choose>
                                    <c:when test="${!scanListResponse.isFirstPage()}">
                                        <button class="btn btn-primary">Previous Page</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-primary" disabled>Previous Page</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                            <form action="scans" method="get" class="scan-manager-page-nav-button">
                                <input type="hidden" name="page" value="${scanListResponse.currentPage + 1}"/>
                                <c:choose>
                                    <c:when test="${!scanListResponse.isLastPage()}">
                                        <button class="btn btn-primary">Next Page</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-primary" disabled>Next Page</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                            <form action="scans" method="get" class="scan-manager-page-nav-button">
                                <input type="hidden" name="page" value="${scanListResponse.totalPages}"/>
                                <c:choose>
                                    <c:when test="${!scanListResponse.isLastPage()}">
                                        <button class="btn btn-primary">Last</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-primary" disabled>Last</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </div>
                        <div class="col-md-6">
                            <p style="padding-top: 14px; text-align: right">Page ${scanListResponse.currentPage} of
                                    ${scanListResponse.totalPages}</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div>
                        <h4>No Scans found</h4>
                    </div>
                </c:otherwise>
            </c:choose>
            <br>
        </div>
    </div>
    <div class="row">
        <hr>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="row" id="preparing-scans">
                <div class="page-header">
                    <h2>Scans Under Preparation</h2>
                </div>
            </div>
            <hr>
            <c:choose>
                <c:when test="${preparingScanList.size() gt 0}">
                    <c:forEach begin="0" end="${preparingScanList.size() - 1}" var="index">
                        <div class="row scan-manager-background-grey">
                            <div class="col-md-8" style="margin-bottom: 15px;">
                                <b>Name: </b>${e:forHtml(preparingScanList.get(index).name)}</br>
                                <b>Scanner: </b> ${preparingScanList.get(index).scannerName}</br>
                                <b>Product: </b> ${preparingScanList.get(index).product}</br>
                                <b>Status: </b> ${preparingScanList.get(index).status}
                            </div>
                            <div class="col-md-4">
                                <c:if test="${preparingScanList.get(index).status.name().equals('ERROR')}">
                                    <form action="clear" method="post"
                                          style="float: right; margin-left: 6px;">
                                        <input type="hidden" name="jobId"
                                               value="${preparingScanList.get(index).jobId}"/>
                                        <button class="btn btn-blue-grey">Clear</button>
                                    </form>
                                </c:if>
                                <form action="logs" method="get"
                                      style="float: right; margin-left: 6px;">
                                    <input type="hidden" name="jobId"
                                           value="${preparingScanList.get(index).jobId}"/>
                                    <button class="btn btn-warning">Logs</button>
                                </form>
                            </div>
                        </div>
                        <br>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div>
                        <h4>No Scans found</h4>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
<%@ include file="../fragments/footer.jsp" %>
</body>
</html>
