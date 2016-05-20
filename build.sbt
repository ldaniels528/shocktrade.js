import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val meanjsVersion = "0.1.10"

val myScalaVersion = "2.11.8"
val myAkkaVersion = "2.4.2"
val myPlayVersion = "2.4.6"
val myAppVersion = "0.8.1"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

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
      // MEANS.js
      "com.github.ldaniels528" %%% "means-angularjs-core" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-animate" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-cookies" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-facebook" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-nervgh-fileupload" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-sanitize" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-toaster" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-ui-bootstrap" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-angularjs-ui-router" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-social-facebook" % meanjsVersion,
      "com.github.ldaniels528" %%% "means-social-linkedin" % meanjsVersion,
      // ScalaJS Libs
      "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
      "com.vmunier" %% "play-scalajs-sourcemaps" % "0.1.0" exclude("com.typesafe.play", "play_2.11"),
      "org.scala-js" %%% "scalajs-dom" % "0.9.0"
    ))
  .enablePlugins(ScalaJSPlugin)

lazy val appSvc = (project in file("app-svc"))
  .settings(
    name := "shocktrade-services",
    organization := "com.shocktrade",
    version := myAppVersion,
    scalaVersion := myScalaVersion,
    libraryDependencies ++= Seq(
      //
      // Shocktrade/ldaniels528 dependencies
      //
      "com.github.ldaniels528" %% "tabular" % "0.1.3" exclude("org.slf4j", "slf4j-log4j12"),
      //
      // Third Party dependencies
      //
      "commons-beanutils" % "commons-beanutils" % "1.9.1",
      "joda-time" % "joda-time" % "2.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
      "net.databinder.dispatch" %% "dispatch-tagsoup" % "0.11.0",
      "net.liftweb" %% "lift-json" % "3.0-M3",
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.joda" % "joda-convert" % "1.6",
      "org.slf4j" % "slf4j-api" % "1.7.10",
      //
      // Testing Dependencies
      //
      "junit" % "junit" % "4.11" % "test"
    )
  )

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
      (compile in Compile) dependsOn (fastOptJS in(appScalaJs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    libraryDependencies ++= Seq(cache, filters, jdbc, json, ws,
      //
      // Shocktrade/ldaniels528 dependencies
      //
      "com.github.ldaniels528" %% "commons-helpers" % "0.1.2",
      "com.github.ldaniels528" %% "tabular" % "0.1.3" exclude("org.slf4j", "slf4j-log4j12"),
      //
      // TypeSafe dependencies
      //
      "com.typesafe.akka" %% "akka-testkit" % myAkkaVersion % "test",
      "com.typesafe.play" %% "play" % myPlayVersion,
      "com.typesafe.play" %% "anorm" % "2.4.0",
      //
      // Third Party dependencies
      //
      "com.github.jsonld-java" % "jsonld-java" % "0.7.0",
      "com.microsoft.sqlserver" % "sqljdbc4" % "4.0",
      "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
      "org.imgscalr" % "imgscalr-lib" % "4.2",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      //
      // Web Jar dependencies
      //
      "org.webjars" % "jquery" % "1.11.1",
      "org.webjars" % "angularjs" % "1.5.2",
      "org.webjars" % "angularjs-nvd3-directives" % "0.0.7-1",
      "org.webjars" % "angularjs-toaster" % "0.4.8",
      //	"org.webjars" % "angular-file-upload" % "1.6.12",
      //	"org.webjars" % "angular-translate" % "2.3.0",
      "org.webjars" % "angular-ui-bootstrap" % "0.12.1-1",
      "org.webjars" % "angular-ui-router" % "0.2.13",
      "org.webjars" % "bootstrap" % "3.3.6",
      "org.webjars" % "d3js" % "3.5.3",
      "org.webjars" % "font-awesome" % "4.5.0",
      "org.webjars" % "jquery" % "2.1.3",
      //	"org.webjars" % "less" % "2.5.0",
      //  "org.webjars" % "textAngular" % "1.2.0",
      "org.webjars" % "nvd3" % "1.8.1",
      "org.webjars" %% "webjars-play" % "2.4.0-2"
    ))
  .dependsOn(appSvc)
  .enablePlugins(PlayScala, play.twirl.sbt.SbtTwirl, SbtWeb)
  .aggregate(appScalaJs)

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project appPlay", _: State)) compose (onLoad in Global).value
