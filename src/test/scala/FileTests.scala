import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import java.io.File

import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._

class FileTests extends FunSuite with BeforeAndAfter {

	before {
		// val nPath = global.require("path")
		// println(nPath.resolve("."))
	}

	test("separators") {
		val file = new File("///home/roger/settings//")
		assert(file.toString == "/home/roger/settings")
	}

	test("separators parent-child") {
		val file = new File("/home/roger//", "/settings")
		assert(file.toString == "/home/roger/settings")
	}

	test("File+child constructor path") {
		val parentFile = new File("/home/roger/EPFL-MA/.././..///")
		val file = new File(parentFile, "/EPFL-MA/")
		assert(file.toString == "/home/roger/EPFL-MA/.././../EPFL-MA")
	}

	test("getCanonicalPath") {
		val file = new File("../.././HCI")
		assert(file.getCanonicalPath == "/home/roger/EPFL-MA/HCI")
	}

	test("length") {
		val file = new File("/home/roger/settings")
		assert(file.length == 79L)
	}

	test("exists : nonexistant directory") {
		val file = new File("/home/unknown")
		assert(!file.exists)
	}

	test("exists : existing file") {
		val file = new File("/home/roger/settings")
		assert(file.exists)
	}

	test("isAbsolute : absolute path") {
		val file = new File("/home/roger")
		val file2 = new File(".")
		assert(file.isAbsolute && !file2.isAbsolute)
	}

	test("isFile : existing file") {
		val file = new File("/home/roger/settings")
		assert(file.isFile)
	}

	test("isDirectory : existing directory") {
		val file = new File("/home/roger/EPFL-MA")
		assert(file.isDirectory)
	}
	
}