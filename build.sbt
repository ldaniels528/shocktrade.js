import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.Play.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val myScalaVersion = "2.11.7"
val myAkkaVersion = "2.3.14"
val myPlayVersion = "2.4.6"
val myAppVersion = "0.8.1"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

val scalajsOutputDir = Def.settingKey[File]("Directory for Javascript files output by ScalaJS")

lazy val appScalaJs = (project in file("app-js"))
  .settings(
    name := "shocktrade-scalajs",
    organization := "com.shocktrade",
    version := myAppVersion,
    scalaVersion := myScalaVersion,
    relativeSourceMaps := true,
    persistLauncher := true,
    persistLauncher in Test := false,
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalascript" % "0.2.20",
      "com.vmunier" %% "play-scalajs-sourcemaps" % "0.1.0" exclude("com.typesafe.play", "play_2.11"),
      "org.scala-js" %%% "scalajs-dom" % "0.9.0",
      "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
    ))
  .enablePlugins(ScalaJSPlugin)

lazy val appPlay = (project in file("app-play"))
  .settings(
    name := "shocktrade.js",
    organization := "com.shocktrade",
    version := myAppVersion,
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
    libraryDependencies ++= Seq(cache, filters, json, ws,
      // Shocktrade/ldaniels528 dependencies
      //
      "com.github.ldaniels528" %% "commons-helpers" % "0.1.2",
      "com.github.ldaniels528" %% "tabular" % "0.1.3" exclude("org.slf4j", "slf4j-log4j12"),
      "com.shocktrade" %% "shocktrade-services" % "0.5",
      //
      // TypeSafe dependencies
      //
      "com.typesafe.akka" %% "akka-testkit" % myAkkaVersion % "test",
      //
      // Third Party dependencies
      //
      "com.github.jsonld-java" % "jsonld-java" % "0.7.0",
      "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
      "org.imgscalr" % "imgscalr-lib" % "4.2",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
      "org.mindrot" % "jbcrypt" % "0.3m",
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
      "org.webjars" %% "webjars-play" % "2.4.0-1",
      "org.webjars" % "bootstrap" % "3.1.1-2"
    ))
  .enablePlugins(PlayScala, play.twirl.sbt.SbtTwirl, SbtWeb)
  .aggregate(appScalaJs)

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project appPlay", _: State)) compose (onLoad in Global).value
