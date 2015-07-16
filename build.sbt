import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.Play.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

name := "shocktrade.js"

organization := "shocktrade.com"

val myScalaVersion = "2.11.6"
val myAkkaVersion = "2.3.9"
val myPlayVersion = "2.3.8" //2.4.0-M3"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

//val clients = Seq(appScalaJs)

val scalajsOutputDir = Def.settingKey[File]("Directory for Javascript files output by ScalaJS")

lazy val appScalaJs = (project in file("app-js"))
  .settings(
    name := "shocktrade-scalajs",
    scalaVersion := myScalaVersion,
    relativeSourceMaps := true,
    persistLauncher := true,
    persistLauncher in Test := false,
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalascript" % "0.1.1",
//    "org.scala-js" %% "scalajs-library" % "0.6.3",
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "be.doeraene" %%% "scalajs-jquery" % "0.8.0"
    ))
  .enablePlugins(ScalaJSPlugin)
//.aggregate(appShared)

lazy val appScalaJvm = (project in file("app-play"))
  .settings(
    name := "shocktrade.js",
    version := "0.8.0",
    scalaVersion := myScalaVersion,
    relativeSourceMaps := true,
    scalajsOutputDir := (crossTarget in Compile).value / "classes" / "public" / "javascripts",
    //scalaJSProjects := clients,
    pipelineStages := Seq(/*scalaJSProd,*/ gzip),
    // ask scalajs project to put its outputs in scalajsOutputDir
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(appScalaJs, Compile, packageJSKey) := scalajsOutputDir.value
    },
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in (appScalaJs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    libraryDependencies ++= Seq(cache, filters, ws,
      // Shocktrade/ldaniels528 dependencies
      //
      "com.ldaniels528" %% "commons-helpers" % "0.1.0",
      "com.ldaniels528" %% "play-json-compat" % "0.1.0",
      "com.ldaniels528" %% "shocktrade-services" % "0.4.4",
      "com.ldaniels528" %% "tabular" % "0.1.2",
      //
      // TypeSafe dependencies
      //
      "com.typesafe.akka" %% "akka-testkit" % myAkkaVersion % "test",
      //  "com.typesafe.play" %% "filters-helpers" % myPlayVersion
      //  "com.typesafe.play" %% "play" % myPlayVersion,
      "com.typesafe.play" %% "play-ws" % myPlayVersion,
      "com.typesafe.play" %% "twirl-api" % "1.0.4",
      //
      // Third Party dependencies
      //
      "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
      "com.vmunier" %% "play-scalajs-scripts" % "0.1.0",
      //
      // Web Jar dependencies
      //
      "org.webjars" % "jquery" % "1.11.1",
      "org.webjars" % "angularjs" % "1.4.1",
      "org.webjars" % "angularjs-nvd3-directives" % "0.0.7-1",
      "org.webjars" % "angularjs-toaster" % "0.4.8",
      //	"org.webjars" % "angular-file-upload" % "1.6.12",
      //	"org.webjars" % "angular-translate" % "2.3.0",
      "org.webjars" % "angular-ui-bootstrap" % "0.12.1-1",
      "org.webjars" % "angular-ui-router" % "0.2.13",
      "org.webjars" % "bootstrap" % "3.3.2-2",
      "org.webjars" % "d3js" % "3.5.3",
      "org.webjars" % "font-awesome" % "4.3.0-2",
      "org.webjars" % "jquery" % "2.1.3",
      //	"org.webjars" % "less" % "2.5.0",
      //  "org.webjars" % "textAngular" % "1.2.0",
      "org.webjars" % "nvd3" % "1.1.15-beta-2",
      "org.webjars" %% "webjars-play" % "2.3.0-3",
      //"org.webjars" %% "webjars-play" % "2.4.0-1",
      "org.webjars" % "bootstrap" % "3.1.1-2"
    ))
  .enablePlugins(play.PlayScala, play.twirl.sbt.SbtTwirl)
  //.aggregate(clients.map(projectToRef): _*)
  .aggregate(appScalaJs)

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project appScalaJvm", _: State)) compose (onLoad in Global).value
