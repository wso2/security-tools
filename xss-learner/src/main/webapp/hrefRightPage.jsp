<%@ page import="org.owasp.encoder.Encode" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String name = request.getParameter( "query" );
%>
<html>
<body>
    <span style="color: #2aa82a;padding-bottom: 10px">
        <h2>You are safe. You have not been attacked. </h2>
    </span>
    <h4 style=" padding-bottom: 10px">  This is the input img src url after encoding. Here we used <i>forUriComponent</i> encoding as a java specific recommendation. </h4>
    <div>
        <span style="padding-bottom: 10px">
            <%=Encode.forUriComponent(name) %>
        </span>
    </div>
    <span style="font-family: Courier; padding-bottom: 20px">
            <a href="http://owasp.github.io/owasp-java-encoder/encoder/apidocs/index.html?index-all.html">For more Info follow forUriComponent encoding method..</a>
    </span>
    <div>
        <form action="hrefAttackLesson.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
    <div>
        <form action="index.jsp" method="get">
            <input type="submit" value="Home" />
        </form>
    </div>
</body>
</html>