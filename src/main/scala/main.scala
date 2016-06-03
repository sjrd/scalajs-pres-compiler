import scala.scalajs.js.JSApp
import fiddle.Classpath
import fiddle.Compiler
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.scalajs.js
import js.Dynamic.{global => g}
import js.annotation.JSExport
import scala.annotation.meta.field
import org.scalajs.dom.document

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
  def column = editor.getCursorPosition().column.asInstanceOf[Int]
*/

object ScalaJSAutocompleter extends JSApp {
	println("Initializing...")
	@JSExport
	val fileLoader = new Classpath

	@JSExport
	def askAutocompletion(editor: js.Dynamic) {
    val code = editor.getValue().asInstanceOf[String]
    val row = editor.getCursorPosition().row.asInstanceOf[Int]
  	val column = editor.getCursorPosition().column.asInstanceOf[Int]
    val intOffset = column + code.split("\n").take(row).map(_.length + 1).sum
    val flag = if(code.take(intOffset).endsWith(".")) "member" else "scope"
		try {
			val compiler = new Compiler(fileLoader)
			val future = compiler.autocomplete(code, flag, intOffset)
			future onComplete {
				case Success(possibleCompletions) => {
					var listItems = new StringBuilder
					for (c <- possibleCompletions) {
						listItems ++= s"<li>${c._2}${c._1}</li>"
					}
					document.getElementById("completions").innerHTML = listItems.toString
					println("autocompletion done")
				}
				case Failure(t) => t.printStackTrace
			}
		} catch {
			case th: Throwable => th.printStackTrace
		}
	}

	def main(): Unit = { }
}