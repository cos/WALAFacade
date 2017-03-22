// set the name of the project
name := "WALAFacade"

version := "0.1.3-SNAPSHOT"

organization := "edu.illinois.wala"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.5", "2.11.6")

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

resolvers += "Caius" at "http://releases.ivy.brindescu.com" // for com.ibm.wala.cast.java

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.+" % "test",
	"com.typesafe" % "config" % "0.5.+",
	"com.ibm.wala" % "com.ibm.wala.shrike" % "1.4.0",
	"com.ibm.wala" % "com.ibm.wala.util" % "1.4.0",
	"com.ibm.wala" % "com.ibm.wala.core" % "1.4.0",
	"com.ibm.wala" % "com.ibm.wala.cast" % "1.4.0",
	"com.ibm.wala" % "com.ibm.wala.cast.java" % "1.4.0"//,
	//"com.ibm.wala" % "com.ibm.wala.ide" % "1.4.0",
	//"com.ibm.wala" % "com.ibm.wala.ide.jdt" % "1.4.0"
)

EclipseKeys.withSource := true

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/cos/WALAFacade</url>
    <licenses>
      <license>
        <name>Eclipse Public License - v 1.0</name>
        <url>https://www.eclipse.org/legal/epl-v10.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/cos/WALAFacade.git</url>
      <connection>scm:git:git://github.com/cos/WALAFacade.git</connection>
    </scm>
    <developers>
      <developer>
        <id>cos</id>
        <name>Cosmin Radoi</name>
        <url>http://cosmin.radoi.net</url>
      </developer>
    </developers>)
