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
        var query='<%=(name)%>';
        displayPosts(query);
    }
</script>
<html>
<body>
    <div>
        <form action="JSValueAttackLesson.jsp" method="get">
            <span style="color: chocolate;padding-bottom: 10px"> Want to know the issue of the code</span>
            <input type="submit" value="YES" />
        </form>
    </div>
    <div id="post-container"></div>
    <div>
        <form action="jsValue.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
</body>
</html>