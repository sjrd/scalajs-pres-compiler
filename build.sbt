// addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.3")

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
		//libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.5.3",
		//libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
		//libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.3" % "provided",
		//libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-can_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-caching_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-http_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-json_2.11" % "1.3.2",
		//libraryDependencies += "io.spray" % "spray-routing_2.11" % "1.3.3",
		//libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.3.8",
		//libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.2.5",
		// libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.7",
		libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.2",
		libraryDependencies += "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5"
		// autoCompilerPlugins := true
	)
	
scalaJSOptimizerOptions ~= { _.withBypassLinkingErrors(true) }