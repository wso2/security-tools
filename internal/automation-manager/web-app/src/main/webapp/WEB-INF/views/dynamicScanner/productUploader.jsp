<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@include file="../fragments/navBar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Dynamic Scanner</h1>
            <h4>Upload a zip file of product or else fill the
                details of already up and running server</h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Upload a zip file</h2>
                <p>Upload a zip file of the product</p>
                <form action="/dynamicScanner/startScan" method="post" enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Zip File</span>
                        <input type="file" name="zipFile" class="form-control" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">URL List File</span>
                        <input type="file" name="urlListFile" class="form-control" required>
                    </div>
                    <br>
                    <input type="hidden" name="productUploadAsZip" value="true">
                    <button class="btn btn-primary btn-block">Submit and Start Scan</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Enter Details of Server</h2>
                <p>Enter Details of up and running server</p>
                <form action="/dynamicScanner/startScan" method="post" enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">IP Address</span>
                        <input name="wso2ServerHost" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Port</span>
                        <input name="wso2ServerPort" class="form-control" type="number" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">URL List File</span>
                        <input type="file" name="urlListFile" class="form-control" required>
                    </div>
                    <br>
                    <input type="hidden" name="productUploadAsZip" value="false">

                    <button class="btn btn-primary btn-block">Submit and Start Scan</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
</body>
</html>
