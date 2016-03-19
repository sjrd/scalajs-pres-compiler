package java.io

import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._

class File(parent: File , child: String) {
	
}

object File {
	
  private val path = global.require("path")
  val pathSeparator: String = path.delimiter.asInstanceOf[String]
  val pathSeparatorChar: Char = pathSeparator(0)
  val separator: String = path.sep.asInstanceOf[String]
  val separatorChar: Char = separator(0)
  
}