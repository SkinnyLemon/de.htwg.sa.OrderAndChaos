import sbt.Keys.libraryDependencies

name          := "de.htwg.sa.orderandchaos"
organization  := "de.htwg.sa"
version       := "0.0.1"
scalaVersion  := "2.13.2"

val commonDependencies = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  libraryDependencies += "junit" % "junit" % "4.8" % "test",
  libraryDependencies += "com.google.inject" % "guice" % "4.1.0",
  libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0",
  libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.1",
  libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.6",
  libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6",
)

lazy val UserInterfaceModule = project.settings(
  name :=  "UserInterfaceModule",
  libraryDependencies ++= commonDependencies
)