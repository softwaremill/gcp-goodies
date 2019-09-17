name := """play-scala-stackdriver-logging"""

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies += "com.google.cloud" % "google-cloud-logging-logback" % "0.108.0-alpha"
libraryDependencies += "com.google.cloud" % "google-cloud-logging" % "1.90.0"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)

sources in (Compile,doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

dockerBaseImage := "anapsix/alpine-java:8_jdk"

dockerRepository := Some("eu.gcr.io/softwaremill-playground-2")

dockerEntrypoint := Seq(
  "bin/play-scala-stackdriver-logging"
//  "-Dlogger.resource=logback_gcp.xml"
)


import ReleaseTransformations._

releaseVersionBump := sbtrelease.Version.Bump.Next

releaseIgnoreUntrackedFiles := true

lazy val dockerRelease : ReleaseStep = { st: State =>
  val extracted = Project.extract(st)
  val ref = extracted.get(thisProjectRef)
  extracted.runAggregated(publish in Docker in ref, st)
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  dockerRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
