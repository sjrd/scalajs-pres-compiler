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
import org.scalajs.dom._

object ScalaJSAutocompleter extends JSApp {
  println("Initializing...")
  @JSExport
  val fileLoader = new Classpath

  def getDocumentCoordinates(element: raw.Element): Map[String, Double] = {
    val boundingRect = element.getBoundingClientRect();
    Map("left" -> (boundingRect.left.asInstanceOf[Double] + window.asInstanceOf[js.Dynamic].scrollX.asInstanceOf[Double]), "top" -> (boundingRect.top.asInstanceOf[Double] + window.asInstanceOf[js.Dynamic].scrollY.asInstanceOf[Double]))
  }

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

          var firstRow = document.querySelector(".container-fluid .row:first-child")
          var cursorElem = document.querySelector(".ace_cursor") // or ".ace_text-input (1 px less for left...)
          var cursorCoords = getDocumentCoordinates(cursorElem)
          var firstRowCoords = getDocumentCoordinates(firstRow)

          var list = document.createElement("ul")
          list.setAttribute("id", "completion-list")
          list.asInstanceOf[js.Dynamic].style.position = "absolute"
          list.asInstanceOf[js.Dynamic].style.left = cursorCoords("left") - firstRowCoords("left") + "px"
          list.asInstanceOf[js.Dynamic].style.top = cursorCoords("top") - firstRowCoords("top") + "px"
          list.innerHTML = listItems.toString

          /*
           * Two important posts from Stackoverflow :
           * http://stackoverflow.com/a/37253012/1829647 (separate conversions to JS functions => removeEventListener had no effect)
           * http://stackoverflow.com/a/27245122/1829647 (foward reference / cannot use arguments.callee with Scala.js)
           */ 
          lazy val closeFunction: js.Function1[js.Dynamic, Unit] = (event: js.Dynamic) => {
            if (event.keyCode.asInstanceOf[Int] == 27) {
              firstRow.removeChild(list)
              document.removeEventListener("keyup", closeFunction)
            }
          }

          document.addEventListener("keyup", closeFunction)
          document.querySelector(".container-fluid .row:first-child").appendChild(list)
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