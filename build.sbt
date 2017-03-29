name := """scala-actor-example"""

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.4.17"

val scalazVersion = "7.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
