enablePlugins(ScalaJSPlugin)
addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.3")

lazy val root = (project in file(".")).
	settings(
		name := "Scala.js presentation compiler",
		version := "1.0",
		scalaVersion := "2.11.7",
		scalaJSUseRhino in Global := false,
		libraryDependencies += "org.scala-js" %% "scalajs-javalib-ex" % scalaJSVersion,
		libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.5.3",
		libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
		libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.3" % "provided",
		//libraryDependencies += "io.spray" % "spray-client_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-can_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-caching_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-http_2.11" % "1.3.3",
		//libraryDependencies += "io.spray" % "spray-json_2.11" % "1.3.2",
		//libraryDependencies += "io.spray" % "spray-routing_2.11" % "1.3.3",
		//libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.3.8",
		//libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.2.5",
		libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.7",
		libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.2",
		libraryDependencies += "org.scala-lang.modules" % "scala-async_2.11" % "0.9.5",
		autoCompilerPlugins := true
	)
	
scalaJSOptimizerOptions ~= { _.withBypassLinkingErrors(true) }
