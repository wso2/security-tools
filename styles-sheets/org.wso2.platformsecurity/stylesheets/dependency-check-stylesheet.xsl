<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:dc="https://jeremylong.github.io/DependencyCheck/dependency-check.1.4.xsd">
  <xsl:template match="/">
    <html>
        <head>
            <style>
                #front-page h1, #front-page h2, #front-page h3, #front-page #info div, #front-page #logo{
                    text-align: center;
                }
                #front-page #info .title{
                    font-weight: 900;
                    padding-right: 5px;
                }
                .item-title {
                    padding-bottom: 20px;
                }
                table {
                    border-collapse: collapse;
                    width: 100%;
                    margin-bottom: 20px;
                }
                table, th, td {
                    border: 1px solid black;
                }
                td {
                    height: 35px;
                    vertical-align: middle;
                    padding: 6px; 
                    white-space: -moz-pre-wrap !important;  /* Mozilla, since 1999 */
                    white-space: -webkit-pre-wrap; /*Chrome  Safari */ 
                    white-space: -pre-wrap;      /* Opera 4-6 */
                    white-space: -o-pre-wrap;    /* Opera 7 */
                    white-space: pre-wrap;       /* css-3 */
                    word-wrap: break-word;       /* Internet Explorer 5.5+ */
                    word-break: break-all;
                    white-space: normal;
                }
                td:first-child {  
                    background-color:#dedede;
                    width: 100px;
                    -webkit-print-color-adjust: exact;
                }
                .item-details {
                    background-color: #E0E0E0;
                    padding: 5px 10px 5px 10px;
                    margin-top: 30px;
                    margin-left: 30px;
                    margin-right: 30px;
                    border-style: dashed;
                    border-width: 2px;
                    font-size: 0.8em;
                    -webkit-print-color-adjust: exact;
                }
                @media print {
                    .break-page { 
                        page-break-after: always;
                        page-break-inside: avoid;
                        -webkit-region-break-inside: avoid;
                    }
                    #front-page #logo {
                        margin-top: 100px;
                    }
                    #front-page #head {
                        margin-top: 100px;
                    }
                    #front-page #info {
                        margin-top: 20%;
                    }
                }
                .hidden {
                	display:none;   
            	}
		.cpe{
			font-style: italic;
			 font-size: small;
    padding: 8px 5px 3px 25px;
    background-color: #f5f5f5;
		}
            </style>
             <script>
                var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
                var reportDate = new Date(<xsl:value-of select="/dc:analysis/dc:projectInfo/dc:reportDate"/>);
                var reportDateElement = document.getElementById('report-date');
                reportDateElement.innerHTML = reportDate.getDate() + ' ' + months[reportDate.getMonth()] + ' ' + reportDate.getFullYear() + ' ' + reportDate.getHours() + ':' + reportDate.getMinutes() + ':' + reportDate.getSeconds() ;

                var titles = document.getElementsByClassName('item-title');
                for(var x = 0; x &lt; titles.length; x++) {
                    var colClass = titles[x].className;
                    console.log(colClass.split(' ')[1]);
                    var classedTitles = document.getElementsByClassName(colClass.split(' ')[1]);
                    for(var y = 1; y &lt; classedTitles.length; y++) {
                        classedTitles[y].style.display = 'none'
                    }
                }
            </script>

            <script>
            function openDetailed(element) { 
                console.log(element);
                element.className = element.className + " hidden";
                element.parentElement.childNodes[2].className = "detailed";
            }
            </script>

        </head>
        <body>
           <div id="front-page">
                <div id="logo">
                    <img src="https://raw.githubusercontent.com/wso2/security-tools/master/resources/images/wso2-logo.jpg" width="400px" alt="WSO2"/>
                </div>
                <div id="head">
                    <h2>Dependency check Report for1<br/><br/> <xsl:value-of select="/dc:analysis/dc:projectInfo/dc:name"/></h2>
                    <h3 style="margin-top:50px;">Version:<br/><br/> <xsl:value-of select="/dc:analysis/dc:projectInfo/dc:version"/></h3>
                    <h2 style="margin-top:50px;">WSO2 Security Assessment</h2>
                </div>
                <div id="info">
                    <div><span class="title">Report Date:</span><span id="report-date"><xsl:value-of select="substring(/dc:analysis/dc:projectInfo/dc:reportDate,1,10)"/></span></div>
                    <div><span class="title">Email:</span><span>security@wso2.com</span></div>
                </div>
            </div>
            <span class="break-page"></span>
            
            <xsl:for-each select="/dc:analysis/dc:dependencies/dc:dependency">
			<xsl:if test="dc:vulnerabilities/dc:vulnerability">
	<div>
				   <h3><xsl:value-of select="dc:fileName"/>   </h3>
	</div>
	<div>
		<h4>Vulnerabilities Found   </h4>
	<table>

					    <tr bgcolor="#4682b4">    
					    <td style="width: 115px;"> CVE </td>
						<td style="width: 115px;"> CVE Score </td>
						<td style="width: 115px;">  Severity </td>
						<td>Description </td>
					    </tr>
				<xsl:for-each select="dc:vulnerabilities/dc:vulnerability">
					<xsl:sort select="dc:cvssScore" order="descending"/>
					<xsl:variable name="mappingNodeVul" select="concat('https://cve.mitre.org/cgi-bin/cvename.cgi?name=',dc:name)"/>					 
					    <tr>    
					    <td><a href="{$mappingNodeVul}"><xsl:value-of select="dc:name"/></a></td>
						<td><xsl:value-of select="dc:cvssScore"/></td>
						<td><xsl:value-of select="dc:severity"/></td>
						<td>
						<xsl:value-of select="dc:description"/>
						
   						<xsl:variable name="cpe" select="dc:vulnerableSoftware/dc:software"/> 						 

						    <div class="cpe">
								<span onclick="openDetailed(this)" class="sum"><xsl:value-of select="$cpe"/></span>
								<span class="detailed hidden">
								<xsl:for-each select="dc:vulnerableSoftware/dc:software">
									<xsl:value-of select="."/> <xsl:text> , </xsl:text>
								</xsl:for-each>
								 </span>
						    </div>
						</td>
						</tr>
		 		</xsl:for-each>
	</table>
	</div>
			</xsl:if>
			<xsl:if test="dc:vulnerabilities/dc:suppressedVulnerability">
	<div>

	<h4>Suppressed Vulnerabilities</h4>

	<table>
					    <tr bgcolor="#4682b4">    
					    <td style="width: 115px;"> CVE </td>
						<td style="width: 115px;"> CVE Score </td>
						<td style="width: 115px;"> Severity </td>
						<td>Description </td>
					    </tr>
    					<xsl:for-each select="dc:vulnerabilities/dc:suppressedVulnerability">
    					<xsl:sort select="dc:cvssScore" order="descending"/>
    					<xsl:variable name="mappingNodeSupVul" select="concat('https://cve.mitre.org/cgi-bin/cvename.cgi?name=',dc:name)"/>	

     						<tr>    
    					    <td><a href="{$mappingNodeSupVul}"><xsl:value-of select="dc:name"/></a></td>
    						<td><xsl:value-of select="dc:cvssScore"/></td>
    						<td><xsl:value-of select="dc:severity"/></td>
    					    <td>
    						<xsl:value-of select="dc:description"/>
       						<xsl:variable name="cpe" select="dc:vulnerableSoftware/dc:software"/> 						 
    						    <div class="cpe">
    								<span onclick="openDetailed(this)" class="sum"><xsl:value-of select="$cpe"/></span>
    								<span class="detailed hidden">
    								<xsl:for-each select="dc:vulnerableSoftware/dc:software">
    									<xsl:value-of select="."/> <xsl:text> , </xsl:text>
    								</xsl:for-each>
    								 </span>
    						    </div>
    						</td>
					    </tr>	
					</xsl:for-each>
	</table>
	</div>
						</xsl:if>
	  </xsl:for-each>
        </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
