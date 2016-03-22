lazy val commonSettings = Seq(
	scalaVersion := "2.11.7"
	// fork in (Compile, run) := true,
	// javaOptions in (Compile, run) += "-Xmx4G",
	// javaOptions in (Compile, run) += "-XX:-UseGCOverheadLimit",
	// javaOptions in (Compile, run) += "-XX:+UseConcMarkSweepGC"
	// javaOptions in (Compile, run) += "-XX:+UseG1GC"
)

lazy val scalaReflectJS = (project in file("sjs-reflect")).
	enablePlugins(ScalaJSPlugin).
	settings(commonSettings: _*).
	settings(
		name := "Scala reflect JS"
	)
	
lazy val scalaCompilerJS = (project in file("sjs-compiler")).
	enablePlugins(ScalaJSPlugin).
	dependsOn(scalaReflectJS).
	settings(commonSettings: _*).
	settings(
		name := "Scala compiler JS",
		libraryDependencies ++= Seq(
			"org.apache.ant" % "ant" % "1.9.6",
			"org.scala-lang.modules" % "scala-asm" % "latest.integration"
		)
	)

lazy val nscToolsInteractive = (project in file("sjs-nsc-interactive")).
	dependsOn(scalaReflectJS, scalaCompilerJS).
	enablePlugins(ScalaJSPlugin).
	settings(commonSettings: _*).
	settings(
		name := "nsc tools interactive"
	)

lazy val root = (project in file(".")).
	aggregate(scalaReflectJS, scalaCompilerJS).
	dependsOn(scalaCompilerJS, nscToolsInteractive).
	enablePlugins(ScalaJSPlugin).
	settings(commonSettings: _*).
	settings(
		name := "Scala.js presentation compiler",
		version := "1.0",
		scalaJSUseRhino in Global := false,
		libraryDependencies += "org.scala-js" %% "scalajs-javalib-ex" % scalaJSVersion,
		libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test"
		// latest.release
		// https://ant.apache.org/ivy/history/2.3.0/ivyfile/dependency.html#revision
	)
	
scalaJSOptimizerOptions ~= { _.withBypassLinkingErrors(true) }