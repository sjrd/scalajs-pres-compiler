package java.io

import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._

class File(pathname: String) {

	private val path = removeExtraneousSeparators(pathname)

	def this(parent: File, child: String) {
		this(parent.getPath + File.separator + child)
	}

	def this(parent: String, child: String) {
		this(parent + File.separator + child)
	}
	
	private def removeExtraneousSeparators(path: String) = {
		val temp = path split(File.separator) filterNot(_ == "") mkString(File.separator)
		if (temp endsWith(File.separator)) temp dropRight(1) else temp
	}
	
	// https://github.com/nodejs/node/issues/1592#issuecomment-98392899
	def exists = {
		try {
			File.nFileSystem.accessSync(path, File.nFileSystem.F_OK)
			true
		} catch {
			case e: Exception => false
		}
	}
	
	def getAbsolutePath = File.nPath.resolve(".") + File.separator + path
	
	def getCanonicalPath = File.nPath.resolve(path).asInstanceOf[String]
	
	def getName = {
		path.split(File.separator).last
	}
	
	def getPath = path
	
	def isDirectory = {
		try {
			val fd = File.nFileSystem.openSync(path, 'r')
			File.nFileSystem.fstatSync(fd).isDirectory().asInstanceOf[Boolean]
		} catch {
			case e: Exception => false
		}
	}
	
	def isFile = {
		try {
			val fd = File.nFileSystem.openSync(path, 'r')
			File.nFileSystem.fstatSync(fd).isFile().asInstanceOf[Boolean]
		} catch {
			case e: Exception => false
		}
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