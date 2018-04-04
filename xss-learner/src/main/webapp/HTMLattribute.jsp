<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>SQL Injection Demo - Search Product</title>
</head>
<style>
    pre{
        font-family: Consolas, Menlo, Monaco, Lucida Console, Liberation Mono, DejaVu Sans Mono, Bitstream Vera Sans Mono, Courier New, monospace, serif;
        margin-left: 5px;
        overflow: auto;
        font-size: 14px;
        width: auto;
        background-color: #eee;
        width: 700px;
        padding: 5px 5px 10px;
        margin-left: 10px;
        max-height: 400px;
        line-height: normal;
    }
</style>
<body>
    <div>
        <h5 style="color:forestgreen; font-size: large">HTML attribute value attack</h5>
        <code>
            <span style=" font-size: xx-large;margin-bottom: 100px">&lt;input value="<span style="color: orange">userInput</span>"></span>
        </code>
    </div>
    <form method="get" action="HTMLAttributeWrongPage.jsp">
        Search: <input name="query"  type="text" value="">
        <input type="submit" value="Submit"  />
    </form>
    <h3>This is the code that is vulnerable...</h3>
    <blockquote>
        <pre >
            <code>
                &lt;input name="query"  type="text" value="">
                &lt;input type="submit" value="Submit"  />
            </code>
        </pre>
        <pre>
            <code>
                &lt;%
                    String name = request.getParameter( "query" );
                %>
                &lt;%= name %>
            </code>
        </pre>
    </blockquote>
    <p id="msgContainer" onclick="getAttackScript() "><span style="color: #0066cc"> Click me to get the attacking script.</span></p>
    <div>
        <form action="index.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
    <script>
        function getAttackScript() {
            document.getElementById("msgContainer").innerHTML = document.getElementById('hiddenMsg').innerHTML;
        }
    </script>
    <script id="hiddenMsg" language="text">
        <h3>In this attack the value attribute can be attacked. But context is matter. So send an<span style="color: #CC8F17"> alert within a script </span> tag considering the context.</h3>
        <h2><span style="color: #CC704C"> <code>
            ">&lt;script>alert('YouHaveBeenAttacked')&lt;/script>
            </code></span> </h2>
    </script>
</body>
</html>