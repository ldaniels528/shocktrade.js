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
	* <a href="#installation">Installing the application</a>
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

* [SBT 0.13+] (http://www.scala-sbt.org/download.html)

<a name="project-dependencies"></a>
### Project Dependencies

* [Transcendent.js 0.2.3.2] (https://github.com/ldaniels528/transcendent.js)

<a name="building-the-code"></a>
### Building the application

    $ sbt fastOptJSCopy

<a name="installation"></a>
### Installing the application
    
The following is an one-time operation to create a working application:
    
    $ node ./daycycle.js --install 
    
The above will create and populate all of the reference data required by the application.    
    
<a name="testing-the-code"></a>    
### Running the application locally

#### To start the web application server 

    $ node ./server.js
       
#### To start the qualification engine

    $ node ./qualification.js

#### To start the day-cycle process

    $ node ./daycycle.js
    
#### To start the trading robots server

    $ node ./robots.js


<img src="https://github.com/ldaniels528/shocktrade.js/blob/master/screenshots/discover.png">
