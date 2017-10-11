<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>XSS Attacks</title>
</head>
<body>
<h5 style="color:darkolivegreen">Select an XSS attack type.</h5>
<table>
    <tr>
        <td style=" font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
        <form method="get" action="HTMLElement.jsp">
        HTML element content Attack
        <input type="submit" value="Submit"  />
    </form>
        </td>
    </tr>
    <tr>
        <td style="font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
        <form method="get" action="HTMLattribute.jsp">
        HTML attribute value Attack
        <input type="submit" value="Submit"  />
    </form>
        </td>
    </tr>
    <tr>
        <td style="font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
        <form method="get" action="jsValue.jsp">
        JavaScript value Attack
        <input type="submit" value="Submit"  />
    </form>
        </td>
    </tr>
    <tr>
        <td style="font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
        <form method="get" action="hrefPage.jsp">
        URL query value Attack
        <input type="submit" value="Submit"  />
    </form>
        </td>
    </tr>
    <tr>
        <td style="font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
        <form method="get" action="domAttack.jsp">
        DOM based Attack
        <input type="submit" value="Submit"  />
    </form>
        </td>
    </tr>
</table>
</body>
</html>
