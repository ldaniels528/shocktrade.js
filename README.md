ShockTrade.js
===============
ShockTrade is a community-oriented securities trading simulation, design to facilitate both education and entertainment
for its participants.

Table of Contents

* <a href="#motivations">Motivations</a>
* <a href="#features">Features</a>
* <a href="#development">Development</a>
	* <a href="#build-requirements">Build Requirements</a>
	* <a href="#project-dependencies">Project Dependencies</a>
	* <a href="#building-the-code">Building the code</a>
	* <a href="#testing-the-code">Running the tests</a>	
	* <a href="#running-the-app">Running the application</a>
	
<a name="motivations"></a>
## Motivations	
	
Learning how to invest can be scary. After all, no one wants to lose money, and without the proper guidance, losing
money is a very real possibility. ShockTrade seeks to change that by allowing users to experiment with investing without
any risk to actual capital. Additionally, the use of social features and the ability to compete with friends changes an
experience could be potentially quite frightening to one that's fun!
	
<a name="features"></a>
## Features

ShockTrade provides the capability of:

* Researching stock securities 
* Simulating stock trading for US Markets (AMEX, NASDAQ, NYSE and OTCBB) in real-time.
* Social Networking
** FaceBook integration
** Interacting with users around the world via the social features of the system.

<a name="development"></a>
## Development

<a name="build-requirements"></a>
### Build Requirements

* [Scala 2.11.8] (http://scala-lang.org/download/)
* [SBT 0.13+] (http://www.scala-sbt.org/download.html)

<a name="project-dependencies"></a>
### Project Dependencies

* [ScalaJs-NodeJs 2.2.x] (https://github.com/ldaniels528/scalajs-nodejs)

<a name="building-the-code"></a>
### Building the web application

    $ sbt fastOptJSPlus
    
<a name="testing-the-code"></a>    
### Running the application locally

#### To start the web server 

    $ node ./server.js
       
#### To start the qualification engine

    $ node ./engine.js


