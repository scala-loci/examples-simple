name := "scala-loci-examples"

organization := "de.tuda.stg"

version := "0.0.0"

scalaVersion := "2.13.2"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint", "-Ymacro-annotations")

resolvers += ("STG old bintray repo" at "http://www.st.informatik.tu-darmstadt.de/maven/").withAllowInsecureProtocol(true)

libraryDependencies ++= Seq(
  "de.tuda.stg" %% "scala-loci-lang" % "0.4.0",
  "de.tuda.stg" %% "scala-loci-serializer-upickle" % "0.4.0",
  "de.tuda.stg" %% "scala-loci-communicator-tcp" % "0.4.0",
  "de.tuda.stg" %% "scala-loci-lang-transmitter-rescala" % "0.4.0")
