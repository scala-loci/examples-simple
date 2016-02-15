name := "retier-examples"

organization := "de.tuda.stg"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "de.tuda.stg" %% "retier-core" % "0+",
  "de.tuda.stg" %% "retier-architectures-basic" % "0+",
  "de.tuda.stg" %% "retier-serializable-upickle" % "0+",
  "de.tuda.stg" %% "retier-network-tcp" % "0+",
  "de.tuda.stg" %% "retier-transmitter-basic" % "0+",
  "de.tuda.stg" %% "retier-transmitter-rescala" % "0+"
)

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint")

addCompilerPlugin("dslparadise" %% "dslparadise" % "0.0.1-SNAPSHOT")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
