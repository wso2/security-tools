<%@ page import="org.owasp.encoder.Encode" %>
<%
    String name = request.getParameter( "query" );
%>
<HTML>
<BODY>
    <td>
        <input type="hidden" id="1" value=<%=name %> />
    </td>
    <span id="message" style="color: orangered;padding-bottom: 10px"> </span>

    <form action="HTMLAttributeAttackLesson.jsp" method="get">
        <span style="color: chocolate;padding-bottom: 10px"> Want to know the issue of the code</span>
        <input type="submit" value="YES" />
    </form>
    <div>
        <form action="HTMLattribute.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
</BODY>
</HTML>