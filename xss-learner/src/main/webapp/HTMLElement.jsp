<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
        <h5 style="color:forestgreen; font-size: large">HTML element content attack</h5>
        <code>
            <span style=" font-size: xx-large;margin-bottom: 100px" > &lt;div><span style="color: orange">userInput</span>&lt;/div></span>
        </code>
    </div>
    <form method="get" action="HTMLElementWrongPage.jsp">
        Search: <input name="query"  type="text">
        <input type="submit" value="Submit"  />
    </form>
    <h3>This is the code that is vulnerable...</h3>
    <blockquote>
    <pre >
            <code>
            &lt;input name="query"  type="text">
            &lt;input type="submit" value="Submit"  />
            </code>
    </pre>
    <pre >
            <code>
            &lt;%
                String name = request.getParameter( "query" );
            %>
            &lt;div>&lt;%= name %>&lt;/div>
            </code>
    </pre>
    </blockquote>

    <script>
        function getAttackScript() {
            document.getElementById("msgContainer").innerHTML = document.getElementById('hiddenMsg').innerHTML;
        }
    </script>
    <script id="hiddenMsg" language="text">
        <h3>This is very simple javascript attack. Just send an<span style="color: #CC8F17"> alert within a script </span> tag.</h3>
        <h2><span style="color: #CC704C"> <code>
            &lt;script>alert('Attacked')&lt;/script>
            </code></span> </h2>
    </script>
    <p id="msgContainer" onclick="getAttackScript() "><span style="color: #0066cc"> Click me to get the attacking script.</span></p>

    <div>
        <form action="index.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
</body>
</html>