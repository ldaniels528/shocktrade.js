name := "shocktrade-js"

organization := "com.ldaniels528"

version := "1.1"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

val akkaVersion = "2.3.9"

val playVersion = "2.3.8" //2.4.0-M3"

libraryDependencies ++= Seq(
  anorm, cache, jdbc, filters, ws
)

// Shocktrade dependencies
libraryDependencies ++= Seq(
	"com.ldaniels528" %% "commons-helpers" % "0.1.0",
	"com.ldaniels528" %% "play-json-compat" % "0.1.0",
	"com.ldaniels528" %% "shocktrade-services" % "0.3.0"
)

// Play dependencies
libraryDependencies ++= Seq(
//  "com.typesafe.play" %% "play" % playVersion,
//  "com.typesafe.play" %% "play-jdbc" % playVersion,
  "com.typesafe.play" %% "play-ws" % playVersion
//  "com.typesafe.play" %% "anorm" % playVersion,
//  "com.typesafe.play" %% "filters-helpers" % playVersion
)

// Third Party dependencies
libraryDependencies ++= Seq(
	"net.liftweb" %% "lift-json" % "2.6",
	"org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
	"org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
)

// Web Jar dependencies
libraryDependencies ++= Seq(
	// top-level scropts
	"org.webjars" % "jquery" % "2.1.3",
	"org.webjars" % "angularjs" % "1.3.15",
	"org.webjars" %% "webjars-play" % "2.3.0-3",
	// all others
	"org.webjars" % "amcharts" % "3.13.1",
	"org.webjars" % "angularjs-toaster" % "0.4.8",
	"org.webjars" % "angular-ui-bootstrap" % "0.12.1-1",
	"org.webjars" % "angular-ui-router" % "0.2.13",
	"org.webjars" % "bootstrap" % "3.1.1", // 3.3.2-2
 	"org.webjars" % "font-awesome" % "4.3.0"
)

// Test Dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
)
