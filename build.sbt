import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

val appVersion = "0.2.0"
val appScalaVersion = "2.12.10"
val scalaJsIOVersion = "0.6.0"

/////////////////////////////////////////////////////////////////////////////////
//      Settings
/////////////////////////////////////////////////////////////////////////////////

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val files = for {
    (base, pname) <- Seq("client" -> "controlpanel", "server" -> "ingestion", "server" -> "qualification", "server" -> "robots", "server" -> "webapp")
    my_dir = out_dir / "app" / base / pname / "target" / s"scala-${appScalaVersion.take(4)}"
    filePair <- Seq("", ".map").map(s"shocktrade-$pname-fastopt.js" + _).map(s => (my_dir / s, out_dir / s))
  } yield filePair
  files foreach { case (src, dest) =>
    IO.copyFile(src, dest, preserveLastModified = false)
  }
}

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

lazy val sharedCommon = (project in file("./app/shared/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

lazy val serverCommon = (project in file("./app/server/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-server-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion
    ))

lazy val dao = (project in file("./app/server/dao"))
  .aggregate(sharedCommon, services)
  .dependsOn(sharedCommon, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-dao",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion
    ))

lazy val facades = (project in file("./app/server/facades"))
  .aggregate(sharedCommon, serverCommon, dao, services)
  .dependsOn(sharedCommon, serverCommon, dao, services)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-facades",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csv-parse" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
    ))

lazy val services = (project in file("./app/server/services"))
  .aggregate(sharedCommon, serverCommon)
  .dependsOn(sharedCommon, serverCommon)
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
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Server-Side Processing projects
/////////////////////////////////////////////////////////////////////////////////

lazy val ingestion = (project in file("./app/server/ingestion"))
  .aggregate(sharedCommon, serverCommon, dao, services, facades)
  .dependsOn(sharedCommon, serverCommon, dao, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-ingestion",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.ingestion.IngestionJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "kafka-node" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val qualification = (project in file("./app/server/qualification"))
  .aggregate(sharedCommon, serverCommon, dao, events, services, facades)
  .dependsOn(sharedCommon, serverCommon, dao, events, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-qualification",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.qualification.QualificationJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Re-think projects
/////////////////////////////////////////////////////////////////////////////////

lazy val events = (project in file("./app/rethink/events"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "event-sourcing",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion
    ))

lazy val persistence = (project in file("./app/rethink/persistence"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(events)
  .settings(moduleSettings: _*)
  .settings(
    name := "persistence",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion
    ))

lazy val neo_qualification = (project in file("./app/rethink/qualification"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(events, persistence)
  .settings(appSettings: _*)
  .settings(
    name := "qualification",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.serverside.qualification.QualificationServer"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "body-parser" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Web Application projects
/////////////////////////////////////////////////////////////////////////////////

lazy val controlPanel = (project in file("./app/client/controlpanel"))
  .aggregate(sharedCommon, serverCommon, services, facades)
  .dependsOn(sharedCommon, serverCommon, services, facades)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "shocktrade-controlpanel",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.controlpanel.ControlPanelJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
    ))

lazy val angularjs = (project in file("./app/client/angularjs"))
  .aggregate(sharedCommon)
  .dependsOn(sharedCommon)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(
    name := "shocktrade-client-angularjs",
    organization := "com.shocktrade",
    version := appVersion,
    mainClass := Some("com.shocktrade.client.WebClientJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIOVersion,
      "io.scalajs.web" %%% "angularjs-v1-bundle" % scalaJsIOVersion
    ))

lazy val webapp = (project in file("./app/server/webapp"))
  .aggregate(sharedCommon, serverCommon, dao, services, facades)
  .dependsOn(sharedCommon, serverCommon, dao, services, facades)
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
      "io.scalajs.npm" %%% "express-csv" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "feedparser-promised" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "md5" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mongodb" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//      Master project
/////////////////////////////////////////////////////////////////////////////////

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, controlPanel, ingestion, neo_qualification, persistence, qualification)
  .dependsOn(angularjs, webapp)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := appScalaVersion,
    //compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    //ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(scalaJSUseMainModuleInitializer, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project shocktradejs", _: State)) compose (onLoad in Global).value
