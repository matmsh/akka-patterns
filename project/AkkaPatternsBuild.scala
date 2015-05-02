import sbt._
import sbt.Keys._

object AkkaPatternsBuild extends Build {

  val akkaVersion = "2.3.10"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % akkaVersion
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.2"
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val mockito = "org.mockito" % "mockito-all" % "1.10.19"

  lazy val akkaPatterns = Project(
    id = "akka-patterns",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
      name := "akka patterns",
      organization := "michaelpollmeier",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.4",
      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      // libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2-SNAPSHOT"
      libraryDependencies ++= Seq(
        akkaActor,
        akkaAgent,
        akkaRemote,
        akkaSlf4j,
        akkaTestkit % "test",
        scalatest % "test",
        slf4j,
        logback,
        mockito % "test"
      )
    )
  )
}
