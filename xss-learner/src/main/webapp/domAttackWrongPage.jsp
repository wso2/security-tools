<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
String name = request.getParameter( "query" );
%>
<script>
    function displayPosts(msg) {
        document.getElementById("msg-container").innerHTML="Hello "+msg+" !";
    }
    window.onload = function() {
        displayPosts('<%=(name)%>');
    }
</script>
<html>
<body>
<div>
    <form action="domAttackLesson.jsp" method="get">
        <span style="color: chocolate;padding-bottom: 10px"> Want to know the issue of the code</span>
        <input type="submit" value="YES" />
    </form>
</div>
<div id="msg-container"></div>
<div>
    <form action="domAttack.jsp" method="get">
        <input type="submit" value="Back" />
    </form>
</div>
</body>
</html>
