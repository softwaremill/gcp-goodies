import com.typesafe.sbt.packager.docker._

name := """play-scala-stackdriver-debugger"""

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += ehcache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.199"

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
  "-source", "1.11",
  "-target", "1.11"
)

javaOptions in Universal ++= Seq(
  // -J params will be added as jvm parameters
  s"-J-agentpath:${(defaultLinuxInstallLocation in Docker).value}/opt/cdbg/cdbg_java_agent.so"
)

sources in (Compile,doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

dockerBaseImage := "openjdk:11-jdk"

dockerRepository := Some("eu.gcr.io/softwaremill-playground-2")

dockerEntrypoint := Seq(
  "bin/play-scala-stackdriver-debugger",
  "-Dconfig.resource=application-prod.conf",
  "-Dcom.google.cdbg.module=play-scala-stacdriver-debugger",
  "-Dcom.google.cdbg.version=1.0.0"
)

dockerCommands ++= Seq(
  // setting the run script executable
  ExecCmd("RUN",
    "mkdir", "-p",
    s"${(defaultLinuxInstallLocation in Docker).value}/opt/cdbg"),
  ExecCmd("RUN",
    "wget", "-q", "-P", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cdbg",
    "https://storage.googleapis.com/cloud-debugger/compute-java/debian-wheezy/cdbg_java_agent_gce.tar.gz"),
  ExecCmd("RUN",
    "tar", "xzvf", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cdbg/cdbg_java_agent_gce.tar.gz",
    "-C", s"${(defaultLinuxInstallLocation in Docker).value}/opt/cdbg")
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
