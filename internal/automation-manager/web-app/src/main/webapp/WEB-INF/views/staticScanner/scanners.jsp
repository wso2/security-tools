<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@include file="../fragments/navBar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Static Scan Methods</h1>
            <h4>Select a scanning method/s to start scanning process </h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <p>Select scanning method/s</p>
                <form action="/staticScanner/productUploader" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-lg">
                        <span class="input-group-addon">
                                <input type="checkbox" name="isFindSecBugs" id="isFindSecBugs">
                            <input type="hidden" name="isFindSecBugs" value="0">
                        </span>
                        <label class="form-control">Find Security Bugs</label>
                    </div>
                    <br>
                    <div class="input-group input-group-lg">
                        <span class="input-group-addon">
                            <input type="checkbox" name="isDependencyCheck" id="isDependencyCheck">
                            <input type="hidden" name="isDependencyCheck" value="0">
                        </span>
                        <label class="form-control">OWASP Dependency Check</label>
                    </div>
                    <br>
                    <button class="btn btn-primary btn-block">Submit</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
</body>
</html>
