<%@ page import="org.owasp.encoder.Encode" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String name = request.getParameter( "query" );
%>
<html>
<body>
    <div>
        <span style="color: #2aa82a;padding-bottom: 20px"> <h2>You are safe. You have not been attacked. </h2></span>
        <span><h4 style=" padding-bottom: 20px">  This is the input query after encoding. Here we used <i>forHtml</i> encoding. </h4></span>
        <td>
           <input type="hidden" id="1" value=<%= Encode.forHtmlAttribute(name) %> />
        </td>
        <span style="font-family: Courier; padding-bottom: 20px">
            <a href="http://owasp.github.io/owasp-java-encoder/encoder/apidocs/index.html?index-all.html">For more Info follow forHtml encoding method..</a>
        </span>
    </div>
    <div>
        <form action="HTMLAttributeAttackLesson.jsp" method="get">
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