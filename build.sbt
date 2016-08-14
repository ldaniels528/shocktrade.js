import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.8.1"

val _scalaVersion = "2.11.8"
val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"
val scalaJsNodeVersion = "0.2.2.4"

scalaJSUseRhino in Global := false

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val inDir = baseDirectory.value / "app-nodejs" / "target" / "scala-2.11"
  val outDir = baseDirectory.value
  val files = Seq("shocktrade-nodejs-fastopt.js", "shocktrade-nodejs-fastopt.js.map") map { p => (inDir / p, outDir / p) }
  IO.copy(files, overwrite = true)
}

lazy val jsCommonSettings = Seq(
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
    "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  ))

lazy val sharedjs = (project in file("./app-shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "shocktrade-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % scalaJsNodeVersion
    ))

lazy val angularjs = (project in file("./app-angularjs"))
  .aggregate(sharedjs)
  .dependsOn(sharedjs)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "shocktrade-angularjs",
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
      "com.github.ldaniels528" %%% "scalajs-angularjs-sanitize" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-toaster" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-bootstrap" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-router" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-social-facebook" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-social-linkedin" % scalaJsNodeVersion
    ))

lazy val nodejs = (project in file("./app-nodejs"))
  .aggregate(sharedjs)
  .dependsOn(sharedjs)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "shocktrade-nodejs",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-elgs-splitargs" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-express-csv" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-feedparser-promised" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-pvorb-md5" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-xml2js" % scalaJsNodeVersion
    ))

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, nodejs)
  .dependsOn(angularjs, nodejs)
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

lazy val tqm = (project in file("./app-tqm"))
  .aggregate(sharedjs)
  .dependsOn(sharedjs)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "shocktrade-tqm",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-elgs-splitargs" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-express-csv" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-feedparser-promised" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-pvorb-md5" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-xml2js" % scalaJsNodeVersion
    ))

// add the alias
addCommandAlias("fastOptJSPlus", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project shocktradejs", _: State)) compose (onLoad in Global).value
