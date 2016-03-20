package java.io

import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._

class File(pathname: String) {

	private val path = pathname // the path string as it was given when calling the constructor

	def this(parent: File, child: String) {
		this(parent.getName + File.separator + child)
	}

	def this(parent: String, child: String) {
		this(parent + File.separator + child)
	}
	
	def exists = {
		
	}
	
	def getAbsolutePath = File.nPath.resolve(".") + File.separator + path
	
	def getCanonicalPath = File.nPath.resolve(path)
	
	def getName = {
		path.split(File.separator).last
	}
	
	def getPath = path
	
	def isFile = {
		
	}
		
	override def toString = path
}

object File {

	private val nFileSystem = global.require("fs")
	private val nPath = global.require("path")
	val pathSeparator: String = nPath.delimiter.asInstanceOf[String]
	val pathSeparatorChar: Char = pathSeparator(0)
	val separator: String = nPath.sep.asInstanceOf[String]
	val separatorChar: Char = separator(0)

}