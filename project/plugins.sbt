
resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("Typesafe Snapshots", url("http://repo.typesafe.com/typesafe/snapshots/"))

resolvers += "bigtoast-github" at "http://bigtoast.github.com/repo/"

addSbtPlugin("com.github.bigtoast" % "sbt-thrift" % "0.7")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

