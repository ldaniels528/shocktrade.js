// Comment to get more information during initialization
logLevel := Level.Info

// Plugins
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.15")

// Resolvers
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("scala-js-snapshots", url("http://repo.scala-js.org/repo/snapshots/"))(Resolver.ivyStylePatterns)
