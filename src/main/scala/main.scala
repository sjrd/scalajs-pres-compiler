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

  // For pop-ups :
  private var selectedIndex = 0;
  private var listLength = 0;

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
          list.setAttribute("tabindex", "0"); // needed for attaching the eventListener to the ul tag
          list.asInstanceOf[js.Dynamic].style.position = "absolute"
          list.asInstanceOf[js.Dynamic].style.left = cursorCoords("left") - firstRowCoords("left") + "px"
          list.asInstanceOf[js.Dynamic].style.top = cursorCoords("top") - firstRowCoords("top") + "px"
          list.innerHTML = listItems.toString

          listLength = list.childElementCount
          selectedIndex = 0;
          list.firstElementChild.classList.add("selected")

          /*
           * Two important posts from Stackoverflow :
           * http://stackoverflow.com/a/37253012/1829647 (separate conversions to JS functions => removeEventListener had no effect)
           * http://stackoverflow.com/a/27245122/1829647 (forward reference / cannot use arguments.callee with Scala.js)
           */ 
          lazy val popupKeyEvent: js.Function1[js.Dynamic, Any] = (event: js.Dynamic) => {
            val keyCode = event.keyCode.asInstanceOf[Int]
            if (keyCode == 27) { // Esc
              list.removeEventListener("keyup", popupKeyEvent) // can probably be removed now that we attach the event listener to the ul tag, not to document anymore !
              firstRow.removeChild(list)
              editor.focus() // this is to put the focus back on the editor after closing the pop-up
            } else if (keyCode == 38) { // Down arrow
              var currentlySelected = list.children(selectedIndex);
              currentlySelected.classList.remove("selected");
              selectedIndex = (selectedIndex - 1 + listLength) % listLength;
              list.children(selectedIndex).classList.add("selected");
            } else if (keyCode == 40) { // Up arrow
              var currentlySelected = list.children(selectedIndex);
              currentlySelected.classList.remove("selected");
              selectedIndex = (selectedIndex + 1) % listLength;
              list.children(selectedIndex).classList.add("selected");
            }
          }

          list.addEventListener("keyup", popupKeyEvent)
          document.querySelector(".container-fluid .row:first-child").appendChild(list)
          document.getElementById("completion-list").asInstanceOf[js.Dynamic].focus();
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