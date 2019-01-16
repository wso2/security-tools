<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
    <%@include file="../fragments/navBar.jsp" %>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Static Scanner</h1>
            <h4>Upload the source code in one of the following ways and select scan/s you want to proceed</h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Upload a zip file</h2>
                <form action="/staticScanner/startScan" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Zip File</span>
                        <input type="file" name="zipFile" id="zipFile" class="form-control">
                    </div>
                    <br>
                    <input type="hidden" name="sourceCodeUploadAsZip" value="true">
                    <button class="btn btn-primary btn-block">Submit & Start Scan</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Clone from GitHub</h2>
                <form action="/staticScanner/startScan" method="post" enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">GitHub URL</span>
                        <input name="gitUrl" id="gitUrl" class="form-control">
                    </div>
                    <br>
                    <input type="hidden" name="sourceCodeUploadAsZip" value="false">
                    <button class="btn btn-primary btn-block">Submit & Start Scan</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
<%@ include file="../fragments/styles.html" %>

</body>
</html>
