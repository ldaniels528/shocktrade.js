import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.1"
val appScalaVersion = "2.12.1"
val transcendentVersion = "0.2.3.4"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val day_dir = out_dir / "app" / "server" / "daycycle" / "target" / "scala-2.12"
  val qual_dir = out_dir / "app" / "server" / "qualification" / "target" / "scala-2.12"
  val robot_dir = out_dir / "app" / "server" / "robots" / "target" / "scala-2.12"
  val web_dir = out_dir / "app" / "server" / "webapp" / "target" / "scala-2.12"
  val cli_dir = out_dir / "app" / "client" / "control_panel" / "target" / "scala-2.12"

  val files1 = Seq("", ".map") map ("shocktrade-daycycle-fastopt.js" + _) map (s => (day_dir / s, out_dir / s))
  val files2 = Seq("", ".map") map ("shocktrade-qualification-fastopt.js" + _) map (s => (qual_dir / s, out_dir / s))
  val files3 = Seq("", ".map") map ("shocktrade-robots-fastopt.js" + _) map (s => (robot_dir / s, out_dir / s))
  val files4 = Seq("", ".map") map ("shocktrade-webapp-fastopt.js" + _) map (s => (web_dir / s, out_dir / s))
  val files5 = Seq("", ".map") map ("shocktrade-controlpanel-fastopt.js" + _) map (s => (cli_dir / s, out_dir / s))
  IO.copy(files1 ++ files2 ++ files3 ++ files4 ++ files5, overwrite = true)
}

lazy val appSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  persistLauncher := true,
  persistLauncher in Test := false,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val moduleSettings = Seq(
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val common = (project in file("./app/shared/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % transcendentVersion
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
      "com.github.ldaniels528" %%% "scalajs-browser-core" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-core" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-anchor-scroll" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-animate" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-cookies" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-facebook" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nervgh-fileupload" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nvd3" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-sanitize" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-toaster" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-bootstrap" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-router" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-social-facebook" % transcendentVersion
    ))

lazy val server_common = (project in file("./app/server/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-server-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-global" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-crypto" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-mongodb" % transcendentVersion
    ))

lazy val webapp = (project in file("./app/server/webapp"))
  .aggregate(common, server_common, dao, services, facades)
  .dependsOn(common, server_common, dao, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-webapp",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-express-csv" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-feedparser-promised" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-md5" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-splitargs" % transcendentVersion
    ))

lazy val control_panel = (project in file("./app/client/control_panel"))
  .aggregate(common, server_common, services, facades)
  .dependsOn(common, server_common, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-controlpanel",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-core" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-fs" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-global" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-repl" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion
    ))

lazy val daycycle = (project in file("./app/server/daycycle"))
  .aggregate(common, server_common, dao, services, facades)
  .dependsOn(common, server_common, dao, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-daycycle",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-kafkanode" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion
    ))

lazy val qualification = (project in file("./app/server/qualification"))
  .aggregate(common, server_common, dao, services, facades)
  .dependsOn(common, server_common, dao, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-qualification",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion
    ))

lazy val robots = (project in file("./app/server/robots"))
  .aggregate(common, server_common, dao, facades)
  .dependsOn(common, server_common, dao, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-robots",
    organization := "com.shocktrade",
    version := appVersion,
    pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-numeral" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion
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
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion
    ))

lazy val services = (project in file("./app/server/services"))
  .aggregate(common, server_common)
  .dependsOn(common, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-services",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-csv-parse" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-xml2js" % transcendentVersion
    ))

lazy val facades = (project in file("./app/server/facades"))
  .aggregate(common, server_common, dao, services)
  .dependsOn(common, server_common, dao, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-facades",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-csv-parse" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-htmlparser2" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-moment-timezone" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-xml2js" % transcendentVersion
    ))

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, daycycle, qualification, robots, control_panel)
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
