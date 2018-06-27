name := "scala-loci-examples"

organization := "de.tuda.stg"

version := "0.0.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint")

resolvers += Resolver.bintrayRepo("stg-tud", "maven")

libraryDependencies ++= Seq(
  "de.tuda.stg" %% "scala-loci-core" % "0.1.0",
  "de.tuda.stg" %% "scala-loci-serializable-upickle" % "0.1.0",
  "de.tuda.stg" %% "scala-loci-network-tcp" % "0.1.0",
  "de.tuda.stg" %% "scala-loci-transmitter-basic" % "0.1.0",
  "de.tuda.stg" %% "scala-loci-transmitter-rescala" % "0.1.0")

addCompilerPlugin("de.tuda.stg" % "dslparadise" % "0.2.0" cross CrossVersion.patch)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
