import sbt.Keys.libraryDependencies

name          := "de.htwg.sa.orderandchaos"
organization  := "de.htwg.sa"
version       := "0.0.1"
scalaVersion  := "2.13.2"

val commonDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "junit" % "junit" % "4.8" % "test",
  "com.google.inject" % "guice" % "4.1.0",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.1",
  "com.typesafe.play" %% "play-json" % "2.6.6",
  "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6"
)

lazy val UserInterfaceModule = project.settings(
  name :=  "UserInterfaceModule",
  libraryDependencies ++= commonDependencies
)
