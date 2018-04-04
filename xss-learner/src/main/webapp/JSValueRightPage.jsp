<%@ page import="org.owasp.encoder.Encode" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String name = request.getParameter( "query" );
%>
<script>
    function displayPosts(msg) {
        document.getElementById("post-container").textContent=msg;
    }
    window.onload = function() {
        var query='<%=(Encode.forJavaScript(name))%>';
        displayPosts(query);
    }
</script>
<html>
<body>
    <span style="color: #2aa82a;padding-bottom: 20px"> <h2>You are safe. You have not been attacked. </h2></span>
    <h4 style=" padding-bottom: 20px">  This is the input query after encoding. Here we used <i>forJavaScript</i> encoding as a java specific recommendation. </h4>
    <span style="margin-bottom: 20px"> <div id="post-container"></div></span>
    <span style="font-family: Courier; padding-bottom: 20px">
            <a href="http://owasp.github.io/owasp-java-encoder/encoder/apidocs/index.html?index-all.html">For more Info follow forJavaScript encoding method..</a>
    </span>
    <div>
        <form action="JSValueAttackLesson.jsp" method="get">
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