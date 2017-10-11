# XssLearner
This project is a web application xss(Cross Site Scripting ) security training platform. XssLearner gives the opportunity to try out different type of xss attacks and follow up the necessary actions to prevent from hacking and to make sure the softwares are secure.

<h2>Download</h2>

<h6>Docker Image</h6>

There is also a docker image available from <a href="https://hub.docker.com/r/nadeeshani/xsslearner/">Dockerhub</a> you can pull it down with

docker pull nadeeshani/xsslearner

Then run,

docker run --rm -p 8080:8080 nadeeshani/xsslearner

or 

<h6>Download war file</h6>

<a href="https://github.com/NShani/XssLearner/blob/master/target/xsslearner.war">download war file</a> and deploy in the tomcat.

<h2>What is the content?</h2>
XSS learner can be used to try out some common xss(cross site scripting) attacks, such as,

  1. HTML element content Attack.
  
      <span style=" font-size: xx-large;margin-bottom: 100px" > &lt;div><span style="color: orange">userInput</span>&lt;/div></span>
      
  2. HTML attribute value Attack.
  
      <span style=" font-size: xx-large;margin-bottom: 100px">&lt;input value="<span style="color: orange">userInput</span>"></span>
      
  3. JavaScript value Attack.
  
      js Method("userInput")
      
  4. URL query value Attack.
  
      &lt;img src ="userInput">
      
      &lt;a href="userInput">
      
  5. DOM based Attack.
  
     <span style=" font-size: xx-large;margin-bottom: 100px" > &lt;div><span style="color: orange">userInput</span>&lt;/div></span>

Then follow up the security guidelines to prevent each type of attacks by using <a href="https://github.com/OWASP/owasp-java-encoder">OWASP Java Encoder</a> . 

XssLearner gives the opportunity to attack to a vulnerable code. Then it shows the secure way to follow when develop the software.
