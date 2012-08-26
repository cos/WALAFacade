// set the name of the project
name := "WALAFacade"

version := "1.0"

organization := "IBM"

scalaSource in Compile <<= baseDirectory(_ / "src")

libraryDependencies += "junit" % "junit" % "4.+"
