import com.typesafe.sbt.packager.docker._

name := """play-scala-stackdriver-profiler"""

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += ehcache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies += "jp.co.bizreach" %% "play-zipkin-tracing-play26" % "2.0.1"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  //  "-Ywarn-unused-import",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions"
)

javacOptions in compile ++= Seq(
  "-encoding", "UTF-8",
  "-source", "1.8",
  "-target", "1.8"
)

javaOptions in Universal ++= Seq(
  // -J params will be added as jvm parameters
  s"-J-agentpath:${(defaultLinuxInstallLocation in Docker).value}/opt/cprof/profiler_java_agent.so=-cprof_service=stackdriver-test4-profiler,-cprof_service_version=1.0.0"
)

sources in (Compile,doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

dockerBaseImage := "openjdk:11-jdk"

dockerRepository := Some("eu.gcr.io/softwaremill-playground-2")

dockerEntrypoint := Seq(
  "bin/play-scala-stackdriver-profiler",
  "-Dconfig.resource=application-prod.conf"
)

dockerCommands ++= Seq(
  // setting the run script executable
  ExecCmd("RUN",
    "mkdir", "-p",
    s"${(defaultLinuxInstallLocation in Docker).value}/opt/cprof"),
  ExecCmd("RUN",
    "wget", "-q", "-P", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cprof",
    "https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz"),
  ExecCmd("RUN",
    "tar", "xzvf", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cprof/profiler_java_agent.tar.gz",
    "-C", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cprof")
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
