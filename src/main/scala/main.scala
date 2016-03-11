import scala.scalajs.js.JSApp
import fiddle.Classpath
import fiddle.Compiler
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/* Code copié depuis Client.scala
def complete() = async {
	log("Completing... ")
	val code = editor.sess.getValue().asInstanceOf[String]
	val intOffset = editor.column + code.split("\n").take(editor.row).map(_.length + 1).sum
	val flag = if(code.take(intOffset).endsWith(".")) "member" else "scope"
	val res = await(Post[Api].completeStuff(code, flag, intOffset).call())
	log("Done")
	logln()
	res
} */
/*
	// dans Editor.scala. Les row et column de l'objet JS renvoyé par getCursorPosition commencent à (0, 0)
	// pour un curseur tout en haut à gauche
	def row = editor.getCursorPosition().row.asInstanceOf[Int]
  def column= editor.getCursorPosition().column.asInstanceOf[Int]
*/

object TutorialApp extends JSApp {
	def main(): Unit = {
		println("Hello world!")
		val exampleCode = """var x: Int = 0; x"""
		val flag = "member"
		val offset = 17 // commencer à compter à partir de 0
		val future = Compiler.autocomplete(exampleCode, flag, offset)
		
		future onComplete {
			case Success(possibleCompletions) => for (c <- possibleCompletions) println(c)
			case Failure(t) => t.printStackTrace
		}
	}
}
