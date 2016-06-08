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

object ScalaJSAutocompleter extends JSApp {
	println("Initializing...")
	@JSExport
	val fileLoader = new Classpath

	@JSExport
	def askAutocompletion(editor: js.Dynamic) {
    val code = editor.getValue().asInstanceOf[String]
    /* The row and column in the JS object returned by getCursorPosition
     * start at (0, 0) for a cursor positioned completely on the left on
     * the top line
		 */
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