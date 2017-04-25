import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.2"
val appScalaVersion = "2.12.3"
val scalaJsIOVersion = "0.4.1"

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

lazy val uiSettings = jsCommonSettings ++ Seq(
  scalaJSUseMainModuleInitializer := true
)

lazy val appSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := true
)

lazy val moduleSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := false
)

lazy val common = (project in file("./app/shared/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade-common",
    organization := "com.shocktrade",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

lazy val angularjs = (project in file("./app/client/angularjs"))
  .aggregate(common)
  .dependsOn(common)
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
      "io.scalajs.npm" %%% "angular-bundle" % scalaJsIOVersion
    ))

lazy val server_common = (project in file("./app/server/common"))
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

lazy val webapp = (project in file("./app/server/webapp"))
  .aggregate(common, server_common, dao, services, facades)
  .dependsOn(common, server_common, dao, services, facades)
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
      "io.scalajs.npm" %%% "express-csv" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "feedparser-promised" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "md5" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIOVersion
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
    mainClass := Some("com.shocktrade.controlpanel.ControlPanelJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
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
    mainClass := Some("com.shocktrade.daycycle.DayCycleJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "kafka-node" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
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
    mainClass := Some("com.shocktrade.qualification.QualificationJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
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
    mainClass := Some("com.shocktrade.autonomous.AutonomousTradingJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "numeral" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion
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
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion
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
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csv-parse" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
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
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csv-parse" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
    ))

lazy val shocktradejs = (project in file("."))
  .aggregate(angularjs, webapp, daycycle, qualification, robots, control_panel)
  .dependsOn(angularjs, webapp)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := appVersion,
    scalaVersion := appScalaVersion,
    //mainClass := Some("com.shocktrade.webapp.WebServerJsApp"),
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(scalaJSUseMainModuleInitializer, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project shocktradejs", _: State)) compose (onLoad in Global).value
