import sbt.Keys.libraryDependencies

ThisBuild / name := "de.htwg.sa.orderandchaos"
ThisBuild / organization := "de.htwg.sa"
ThisBuild / version := "0.0.1"
ThisBuild / scalaVersion := "2.12.4"

val akkaVersion = "2.5.26"
val akkaHttpVersion = "10.1.11"

val commonDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "junit" % "junit" % "4.8" % "test",
  "com.google.inject" % "guice" % "4.1.0",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.1",
  "com.typesafe.play" %% "play-json" % "2.6.6",
  "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.4.199"
)

lazy val global = project
  .in(file("."))
  .aggregate(
    OacWinChecker,
    OacUi,
    OacData
  )

lazy val OacUi = project.settings(
  name := "OacUi",
  libraryDependencies ++= commonDependencies,
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "module-info.class" => MergeStrategy.concat
    case "CHANGELOG.adoc" => MergeStrategy.concat
    case "unwanted.txt" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  assemblyJarName in assembly := "OacUi.jar",
  mainClass in assembly := Some("de.htwg.se.orderandchaos.ui.UiModule")
)

lazy val OacData = project.settings(
  name := "OacData",
  libraryDependencies ++= commonDependencies,
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "module-info.class" => MergeStrategy.concat
    case "CHANGELOG.adoc" => MergeStrategy.concat
    case "unwanted.txt" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  assemblyJarName in assembly := "OacData.jar",
  mainClass in assembly := Some("de.htwg.se.orderandchaos.data.DataModule")
)

lazy val OacWinChecker = project.settings(
  name := "OacWinChecker",
  libraryDependencies ++= commonDependencies,
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "module-info.class" => MergeStrategy.concat
    case "CHANGELOG.adoc" => MergeStrategy.concat
    case "unwanted.txt" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  assemblyJarName in assembly := "OacWinChecker.jar",
  mainClass in assembly := Some("de.htwg.se.orderandchaos.winconditionchecker.WinCheckerModule")
)
