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
    <h5 style="color:forestgreen; font-size: large">URl attack</h5>
    <table style="padding: 0 0; width: 100%; background: #ffffff;">
        <tr>
            <td colspan="2"
                style="padding: 5% 7%; font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
                <div>
                    <code>
                        <span style=" color: darkblue" > &lt;img <span style="color: blue" >src</span> ="<span style="color: orange">userInput</span>"></span>
                    </code>
                </div>
                <form method="get" action="imgSrcWrongPage.jsp">
                    Enter the image number: <input name="query"  type="text">
                    <input type="submit" value="Submit"  />
                </form>
                <h3>This is the code that is vulnerable... There are 1.png,2.png and 3.png in the project..</h3>
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
                            &lt;img src="static/images/&lt;%=name%>.png">
                        </code>
                    </pre>
                </blockquote>
                <p id="msgContainer" onclick="getAttackScript1() "><span style="color: #0066cc"> Click me to get the attacking script.</span></p>
            </td>
        </tr>
        <tr>
            <td colspan="2"
                style="padding: 5% 7%; font-size: 16px; line-height: 30px; color: #676767; border-top: 1px solid #cccccc; padding-top: 15px; padding-bottom: 15px;">
                <div>
                    <code>
                        <span style="color: darkblue" > &lt;a <span style="color: blue" >href</span>="<span style="color: orange">userInput</span>"></span>
                    </code>
                </div>
                <form method="get" action="hrefWrongPage.jsp">
                    Enter the image number: <input name="query"  type="text">
                    <input type="submit" value="Submit"  />
                </form>
                <pre >
                    <code>
                        &lt;input name="query"  type="text">
                        &lt;input type="submit" value="Submit"  />
                    </code>
                </pre>
                <pre>
                    <code>
                        &lt;%
                            String name = request.getParameter( "query" );
                        %>
                        &lt;a href="static/images/&lt;%=name%>.png">Click me to see the image</a>
                    </code>
                </pre>
                <p id="msgContainer1" onclick="getAttackScript2() "><span style="color: #0066cc"> Click me to get the attacking script.</span></p>
            </td>
        </tr>
    </table>
    <div>
        <form action="index.jsp" method="get">
            <input type="submit" value="Back" />
        </form>
    </div>
    <script>
            function getAttackScript1() {
                document.getElementById("msgContainer").innerHTML = document.getElementById('hiddenMsg1').innerHTML;
            }
            function getAttackScript2() {
                document.getElementById("msgContainer1").innerHTML = document.getElementById('hiddenMsg2').innerHTML;
            }
    </script>
    <script id="hiddenMsg1" language="text">
            <h3>This is a src url attack. using &lt;script> as a payload won't work. Thus look at the vulnerable context and inject a alert.</h3>
            <h2>
                <span style="color: #CC704C">
                    <code>
                        1.png" onload=alert('YouHaveBeenHacked')'//>
                    </code>
                </span>
            </h2>
    </script>
    <script id="hiddenMsg2" language="text">
            <h3>This is a url xss attack. using &lt;script> as a payload won't work. Thus look at the vulnerable context and inject a alert.</h3>
            <h2>
                <span style="color: #CC704C">
                    <code>
                        ">&lt;/a>&lt;a href = "javascript:alert('YouHaveBeenHacked');"
                    </code>
                </span>
            </h2>
    </script>
</body>
</html>