<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="../fragments/header.html" %>
    <%@include file="../fragments/navBar.jsp" %>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>My Scans History</h1>
        </div>
        <div class="col-lg-12">
            <h3>Dynamic Scanners</h3>
            <c:choose>
                <c:when test="${dynamicScanners.length()!=0 && dynamicScanners!=null}">
                    <c:forEach begin="0" end="${dynamicScanners.length()-1}" var="index">
                        <div class="col-lg-4 col-md-4 col-sm-6">
                            <div class="thumbnail">
                                <table class="table table-bordered table-striped table-hover">
                                    <tbody>
                                    <tr>
                                        <th>Test Name</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getString("testName")}</td>
                                    </tr>
                                    <tr>
                                        <th>Container Status</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getString("status")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('createdTime')}">
                                        <tr>
                                            <th>Created Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("createdTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>File Uploaded</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getBoolean("fileUploaded")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('fileUploadedTime')}">
                                        <tr>
                                            <th>File Uploaded Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("fileUploadedTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>File Extracted</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getBoolean("fileExtracted")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('fileExtractedTime')}">
                                        <tr>
                                            <th>File Extracted Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("fileExtractedTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>Server Started</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getBoolean("serverStarted")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('serverStartedTime')}">
                                        <tr>
                                            <th>Server Started Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("serverStartedTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>Report Ready</th>
                                        <td>${dynamicScanners.getJSONObject(index).getBoolean("reportReady")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('reportReadyTime')}">
                                        <tr>
                                            <th>Server Started Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("reportReadyTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>Report Sent</th>
                                        <td>${dynamicScanners.getJSONObject(index).getBoolean("reportSent")}</td>
                                    </tr>
                                    <c:if test="${!dynamicScanners.getJSONObject(index).isNull('reportSentTime')}">
                                        <tr>
                                            <th>Report Sent Time</th>
                                            <td>${dynamicScanners.getJSONObject(index).getString("reportSentTime")}</td>
                                        </tr>
                                    </c:if>
                                    <tr>
                                        <th>Zap Scan Progress</th>
                                        <td>
                                            <div class="progress">
                                                <div class="progress-bar progress-bar-success" role="progressbar"
                                                     aria-valuenow="${dynamicScanners.getJSONObject(index).getInt("zapScanProgress")}"
                                                     aria-valuemax="100"
                                                     aria-valuemin="0"
                                                     style="width: ${dynamicScanners.getJSONObject(index).getInt("zapScanProgress")}%;">

                                                </div>
                                            </div>
                                        </td>
                                    </tr>

                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>No dynamic scans are found</p>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="col-lg-12">
            <h3>Static Scanners</h3>
            <c:choose>
                <c:when test="${staticScanners.length()!=0 && staticScanners!=null}">
                    <c:forEach begin="0" end="${staticScanners.length()-1}" var="index">
                        <div class="col-lg-4 col-md-4 col-sm-6">
                            <div class="thumbnail">
                                <table class="table table-bordered table-striped table-hover">
                                    <tbody>
                                    <tr>
                                        <th>Test Name</th>
                                        <td>${staticScanners.getJSONObject(index).getString("testName")}</td>
                                    </tr>
                                    <tr>
                                        <th>Container Status</th>
                                        <td> ${dynamicScanners.getJSONObject(index).getString("status")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('createdTime')}">
                                    <tr>
                                        <th>Created Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("createdTime")}</td>
                                    </tr>
                                    </c:if>
                                    <tr>
                                        <th>File Extracted</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("fileExtracted")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('fileExtractedTime')}">
                                    <tr>
                                        <th>File Extracted Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("fileExtractedTime")}</td>
                                    </tr>
                                    </c:if>

                                    <tr>
                                        <th>Product Cloned</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("productCloned")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('productClonedTime')}">
                                    <tr>
                                        <th>Product Cloned Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("productClonedTime")}</td>
                                    </tr>
                                    </c:if>

                                        <%--<tr>--%>
                                        <%--<th>FindSecBugs Status</th>--%>
                                        <%--<td> ${staticScanners.getJSONObject(index).getString("findSecBugsStatus")}</td>--%>
                                        <%--</tr>--%>
                                    <tr>
                                        <th>FindSecBugs Report Ready</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("findSecBugsReportReady")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('findSecBugsReportReadyTime')}">
                                    <tr>
                                        <th>FindSecBugs Report Ready Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("findSecBugsReportReadyTime")}</td>
                                    </tr>
                                    </c:if>

                                        <%--<tr>--%>
                                        <%--<th>Dependency Check Status</th>--%>
                                        <%--<td> ${staticScanners.getJSONObject(index).getString("dependencyCheckStatus")}</td>--%>
                                        <%--</tr>--%>
                                    <tr>
                                        <th>Dependency Check Report Ready</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("dependencyCheckReportReady")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('dependencyCheckReportReadyTime')}">
                                    <tr>
                                        <th>Dependency Check Report Ready Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("dependencyCheckReportReadyTime")}</td>
                                    </tr>
                                    </c:if>
                                    <tr>
                                        <th>Full Report Ready</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("reportReady")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('reportReadyTime')}">
                                    <tr>
                                        <th>Dependency Check Report Ready Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("reportReadyTime")}</td>
                                    </tr>
                                    </c:if>
                                    <tr>
                                        <th>Report Sent</th>
                                        <td> ${staticScanners.getJSONObject(index).getBoolean("reportSent")}</td>
                                    </tr>
                                    <c:if test="${!staticScanners.getJSONObject(index).isNull('ReportSentTime')}">
                                    <tr>
                                        <th>Dependency Check Report Ready Time</th>
                                        <td>${staticScanners.getJSONObject(index).getString("reportSentTime")}</td>
                                    </tr>
                                    </c:if>
                                </table>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p>No static scans are found</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@include file="../fragments/footer.jsp" %>

</body>
</html>