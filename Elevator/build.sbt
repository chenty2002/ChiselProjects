ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.10"

addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.6" cross CrossVersion.full)
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"
libraryDependencies += "edu.berkeley.cs" %% "chisel-iotesters" % "2.5.6"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.5.6" % "test"
