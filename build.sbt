import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

val appVersion = "0.2.0"
val appScalaVersion = "2.12.11"
val scalaJsIOVersion = "0.6.0"

/////////////////////////////////////////////////////////////////////////////////
//      Settings
/////////////////////////////////////////////////////////////////////////////////

lazy val jsCommonSettings = Seq(
  javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars"),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint"),
  scalacOptions ++= Seq("-feature", "-deprecation", "-P:scalajs:sjsDefinedByDefault"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val appSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := true
)

lazy val moduleSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := false
)

lazy val uiSettings = jsCommonSettings ++ Seq(
  scalaJSUseMainModuleInitializer := true
)

/////////////////////////////////////////////////////////////////////////////////
//      Common projects
/////////////////////////////////////////////////////////////////////////////////

lazy val server_common = (project in file("./app/shared/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-serverside-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion
    ))

lazy val services = (project in file("./app/shared/services"))
  .aggregate(models, server_common)
  .dependsOn(models, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-services",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csv-parse" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val utils = (project in file("./app/shared/utils"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-utils",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Back-end projects
/////////////////////////////////////////////////////////////////////////////////

lazy val onetime = (project in file("./app/backends/onetime"))
  .settings(
    name := "shocktrade-onetime",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := "2.13.1",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    libraryDependencies ++= Seq(
      "log4j" % "log4j" % "1.2.17",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.slf4j" % "slf4j-log4j12" % "1.7.25"
    ))

lazy val ingestion = (project in file("./app/backends/ingestion"))
  .aggregate(models, server_common, services)
  .dependsOn(models, server_common, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-ingest",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.ingestion.IngestionJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val webapp = (project in file("./app/backends/webapp"))
  .aggregate(models, server_common)
  .dependsOn(models, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-webapp",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.webapp.WebServerJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "connect-timeout" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-csv" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "feedparser-promised" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "md5" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Front-end projects
/////////////////////////////////////////////////////////////////////////////////

lazy val cli = (project in file("./app/frontends/cli"))
  .aggregate(models, server_common)
  .dependsOn(models, server_common)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-cli",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.cli.CliJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val angularjs = (project in file("./app/frontends/angularjs"))
  .aggregate(models)
  .dependsOn(models)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(
    name := "shocktrade-angularjs",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.client.WebClientJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIOVersion,
      "io.scalajs.web" %%% "angularjs-v1-bundle" % scalaJsIOVersion,
      "io.scalajs.web" %%% "amcharts" % scalaJsIOVersion
    ))

lazy val models = (project in file("./app/frontends/models"))
  .dependsOn(utils)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-models",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Master project
/////////////////////////////////////////////////////////////////////////////////

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, cli, ingestion)
  .dependsOn(angularjs, webapp)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := appScalaVersion,
    Seq(scalaJSUseMainModuleInitializer, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project shocktradejs", _: State)) compose (onLoad in Global).value
