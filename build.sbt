// set the name of the project
name := "WALAFacade"

version := "0.1"

organization := "University of Illinois"

scalaVersion := "2.10.0"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.+",
	"com.typesafe" % "config" % "0.5.+",
	"com.ibm.wala" % "com.ibm.wala.shrike" % "1.3.4-SNAPSHOT",
	"com.ibm.wala" % "com.ibm.wala.util" % "1.3.4-SNAPSHOT",
	"com.ibm.wala" % "com.ibm.wala.core" % "1.3.4-SNAPSHOT")
	
EclipseKeys.withSource := true