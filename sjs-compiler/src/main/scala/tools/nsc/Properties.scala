/* NSC -- new Scala compiler
 * Copyright 2006-2013 LAMP/EPFL
 * @author  Stephane Micheloud
 */

package scala.tools.nsc

/** Loads `compiler.properties` from the jar archive file.
 */
object Properties extends scala.util.PropertiesTrait {
  protected def propCategory   = "compiler"
  protected def pickJarBasedOn = classOf[Global]
  
  /*
   * There is a getResourceAsStream in scala.util.PropertiesTrait, which does not work
   * with Scala.js. Therefore we override the scalaProps val containing the
   * getResourceAsStream call, and simply return an empty java.util.Properties
   */
  override protected lazy val scalaProps: java.util.Properties = {
    val props = new java.util.Properties
    props.setProperty("version.number", "2.11.7")
    props
  }

  // settings based on jar properties, falling back to System prefixed by "scala."

  // messages to display at startup or prompt, format string with string parameters
  // Scala version, Java version, JVM name
  def residentPromptString = scalaPropOrElse("resident.prompt", "\nnsc> ")
  def shellPromptString    = scalaPropOrElse("shell.prompt", "%nscala> ")
  def shellWelcomeString   = scalaPropOrElse("shell.welcome",
    """Welcome to Scala %1$#s (%3$s, Java %2$s).
      |Type in expressions for evaluation. Or try :help.""".stripMargin
  )

  // message to display at EOF (which by default ends with
  // a newline so as not to break the user's terminal)
  def shellInterruptedString = scalaPropOrElse("shell.interrupted", f":quit$lineSeparator")

  // derived values
  def isEmacsShell         = propOrEmpty("env.emacs") != ""
}
