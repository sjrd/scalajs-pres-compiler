lazy val commonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val scalaReflectJS = (project in file("sjs-reflect")).
  enablePlugins(ScalaJSPlugin). 
  settings(commonSettings: _*).
  settings(
    name := "Scala reflect JS"
  )
  
lazy val scalaCompilerJS = (project in file("sjs-compiler")).
  dependsOn(scalaReflectJS).
  enablePlugins(ScalaJSPlugin).
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
    jsEnv := NodeJSEnv(args = Seq("--stack-size=8192")).value,
    libraryDependencies += "org.scala-js" %% "scalajs-javalib-ex" % scalaJSVersion,
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0-M15" % "test",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    scalaJSSemantics ~= { _.withModuleInit(org.scalajs.core.tools.sem.CheckedBehavior.Compliant) }
    // about dependency revisions : https://ant.apache.org/ivy/history/2.3.0/ivyfile/dependency.html#revision
  )
  
scalaJSOptimizerOptions ~= { _.withBypassLinkingErrors(true) }