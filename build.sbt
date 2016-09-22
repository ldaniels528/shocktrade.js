import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"

val appScalaVersion = "2.11.8"
val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"
val scalaJsNodeVersion = "0.2.3.0"

scalaJSUseRhino in Global := false

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val day_dir = out_dir / "app" / "server" / "daycycle" / "target" / "scala-2.11"
  val qual_dir = out_dir / "app" / "server" / "qualification" / "target" / "scala-2.11"
  val robot_dir = out_dir / "app" / "server" / "robots" / "target" / "scala-2.11"
  val web_dir = out_dir / "app" / "server" / "webapp" / "target" / "scala-2.11"

  val files1 = Seq("", ".map") map ("shocktrade-daycycle-fastopt.js" + _) map (s => (day_dir / s, out_dir / s))
  val files2 = Seq("", ".map") map ("shocktrade-qualification-fastopt.js" + _) map (s => (qual_dir / s, out_dir / s))
  val files3 = Seq("", ".map") map ("shocktrade-robots-fastopt.js" + _) map (s => (robot_dir / s, out_dir / s))
  val files4 = Seq("", ".map") map ("shocktrade-webapp-fastopt.js" + _) map (s => (web_dir / s, out_dir / s))
  IO.copy(files1 ++ files2 ++ files3 ++ files4, overwrite = true)
}

lazy val appSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  persistLauncher := true,
  persistLauncher in Test := false,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  libraryDependencies ++= Seq(
    //  "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % appScalaVersion,
    //
    // Testing dependencies
    //
    "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test"
  ))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  libraryDependencies ++= Seq(
    //  "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % appScalaVersion,
    //
    // Testing dependencies
    //
    "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test"
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-express-csv" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-feedparser-promised" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-md5" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-splitargs" % scalaJsNodeVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % scalaJsNodeVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % scalaJsNodeVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-numeral" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % scalaJsNodeVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % scalaJsNodeVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % scalaJsNodeVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-xml2js" % scalaJsNodeVersion
    ))

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, daycycle, qualification, robots)
  .dependsOn(angularjs, webapp)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := appScalaVersion,
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
