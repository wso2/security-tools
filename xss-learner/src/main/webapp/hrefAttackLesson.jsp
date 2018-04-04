<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
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
    <table>
        <div class="container">
            <div class="row">
                <tr >
                    <td style="line-height: 24px; color: #676767; border-top: 3px solid #cccccc;">
                        <div class="col-xs-4">
                            <h3 style="color: orangered"> This is the wrong html code..</h3>
                            <blockquote>
                                <pre id="WrongCode">
                                    <code>
                                        String name = request.getParameter( <span style="color: #3a802d ">query"</span> );
                                        &lt;<span style="color:rebeccapurple">a</span> <span style="color: blue">href</span><span style="color: forestgreen">="static/images/</span>&lt;%=name%><span style="color: forestgreen">img.png"</span>>Click me to see the image</a>
                                    </code>
                                </pre>
                            </blockquote>
                            <h4>If you want to try out this code enter a query to search</h4>
                            <form method="get" action="hrefWrongPage.jsp">
                                Enter the script here:
                                <input name="query" value="" type="text">
                                <input type="submit" value="Submit" />
                            </form>
                        </div>
                    </td>
                </tr>
                <tr >
                    <td style="line-height: 24px; color: #676767; border-top: 3px solid #cccccc;">
                        <div class="col-xs-4">
                            <h3 style="color: #2aa82a"> This is the right way of encoding in html code..</h3>
                            <blockquote>
                                <pre id="RightCode">
                                    <code>
                                        String name = request.getParameter( <span style="color: #3a802d ">query"</span> );
                                        &lt;<span style="color:rebeccapurple">img</span> <span style="color: blue">src</span><span style="color: forestgreen">="static/images/</span>&lt;%=<span style="color: orange" >Encode.<i>forJavaScript</i></span>(name)%><span style="color: forestgreen">img.png"</span>>
                                    </code>
                                </pre>
                            </blockquote>
                            <h4>If you want to try out this code enter a query to search</h4>
                            <form method="get" action="hrefRightPage.jsp">
                                Enter the vulnerable script here:
                                <input name="query" value="" type="text">
                                <input type="submit" value="Submit" />
                            </form>
                        </div>
                    </td>
                </tr>
            </div>
        </div>
    </table>
    <div>
        <form action="hrefWrongPage.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
</body>
</html>