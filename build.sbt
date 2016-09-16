import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"

val _scalaVersion = "2.11.8"
val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"
val scalaJsNodeVersion = "0.2.2.7"

scalaJSUseRhino in Global := false

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val outDir = baseDirectory.value
  val dayDir = outDir / "app" / "server" / "daycycle" / "target" / "scala-2.11"
  val qualifDir = outDir / "app" / "server" / "qualification" / "target" / "scala-2.11"
  val robotDir = outDir / "app" / "server" / "robots" / "target" / "scala-2.11"
  val webDir = outDir / "app" / "server" / "webapp" / "target" / "scala-2.11"

  val files1 = Seq("shocktrade-daycycle-fastopt.js", "shocktrade-daycycle-fastopt.js.map") map(s => (dayDir / s, outDir / s))
  val files2 = Seq("shocktrade-qualification-fastopt.js", "shocktrade-qualification-fastopt.js.map") map(s => (qualifDir / s, outDir / s))
  val files3 = Seq("shocktrade-robots-fastopt.js", "shocktrade-robots-fastopt.js.map") map(s => (robotDir / s, outDir / s))
  val files4 = Seq("shocktrade-webapp-fastopt.js", "shocktrade-webapp-fastopt.js.map") map(s => (webDir / s, outDir / s))
  IO.copy(files1 ++ files2 ++ files3 ++ files4, overwrite = true)
}

lazy val appSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := _scalaVersion,
  persistLauncher := true,
  persistLauncher in Test := false,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  libraryDependencies ++= Seq(
//  "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  ))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := _scalaVersion,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  libraryDependencies ++= Seq(
    //  "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  ))

lazy val common = (project in file("./app/shared/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % scalaJsNodeVersion
    ))

lazy val angularjs = (project in file("./app/client/angularjs"))
  .aggregate(common)
  .dependsOn(common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-client-angularjs",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      // MEANS.js
      "com.github.ldaniels528" %%% "scalajs-browser-core" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-core" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-anchor-scroll" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-animate" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-cookies" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-facebook" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nervgh-fileupload" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nvd3" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-sanitize" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-toaster" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-bootstrap" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-router" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-social-facebook" % scalaJsNodeVersion
    ))

lazy val webapp = (project in file("./app/server/webapp"))
  .aggregate(common, dao, services)
  .dependsOn(common, dao, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-webapp",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-elgs-splitargs" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-express-csv" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-feedparser-promised" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-pvorb-md5" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion
    ))

lazy val daycycle = (project in file("./app/server/daycycle"))
  .aggregate(common, dao, services)
  .dependsOn(common, dao, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-daycycle",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion
    ))

lazy val qualification = (project in file("./app/server/qualification"))
  .aggregate(common, dao, services)
  .dependsOn(common, dao, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-qualification",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion
    ))

lazy val robots = (project in file("./app/server/robots"))
  .aggregate(common, dao)
  .dependsOn(common, dao)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-robots",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion
    ))

lazy val dao = (project in file("./app/server/dao"))
  .aggregate(common, services)
  .dependsOn(common, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-dao",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment-timezone" % scalaJsNodeVersion
    ))

lazy val services = (project in file("./app/server/services"))
  .aggregate(common)
  .dependsOn(common)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-services",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-xml2js" % scalaJsNodeVersion
    ))

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, daycycle, qualification, robots)
  .dependsOn(angularjs, webapp)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := _scalaVersion,
    relativeSourceMaps := true,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project shocktradejs", _: State)) compose (onLoad in Global).value
