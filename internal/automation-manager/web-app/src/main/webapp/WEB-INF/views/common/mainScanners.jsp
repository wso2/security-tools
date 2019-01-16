<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="../fragments/header.html" %>
</head>
<body>
<%@include file="../fragments/navBar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scanners</h1>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Static Scanner</h2>
                <p>This scanner will accept a zip file or a GitHub url of the source code of the project. Then
                    Static Scanner can start scanning your source code using given static scanners</p>
                <form action="/staticScanner/scanners" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Test Name</span>
                        <input name="testName" class="form-control" placeholder="Please enter a name to test" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Product Name</span>
                        <input name="productName" class="form-control" placeholder="Please enter the product name"
                               required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">WUM Level</span>
                        <input name="wumLevel" class="form-control" placeholder="Please enter WUM level of the product"
                               required>
                    </div>
                    <br>
                    <button name="btnStartStaticScanner" class="btn btn-primary btn-block">
                        Click here to start Static Scanner
                    </button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Dynamic Scanner</h2>
                <p>This scanner will accept a zip file of the product, or url of a running server. Then
                    Dynamic Scanner can start scanning the server using given dynamic scanners</p>
                <form action="/dynamicScanner/scanners" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Test Name</span>
                        <input name="testName" class="form-control" placeholder="Please enter a name to test" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Product Name</span>
                        <input name="productName" class="form-control" placeholder="Please enter the product name"
                               required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">WUM Level</span>
                        <input name="wumLevel" class="form-control" placeholder="Please enter WUM level of the product"
                               required>
                    </div>
                    <br>
                    <button name="btnStartDynamicScanner" class="btn btn-primary btn-block">
                        Click here to start Dynamic Scanner
                    </button>
                </form>
            </div>

        </div>
    </div>
    <%@include file="../fragments/footer.jsp" %>
    <%@ include file="../fragments/styles.html" %>
</body>

</html>