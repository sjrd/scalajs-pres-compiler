package java.io

import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._

class FileInputStream(file: File) extends InputStream {

	private val fs = global.require("fs")
	private val buffer = global.require("buffer")
	private var bf = js.Dynamic.newInstance(buffer.Buffer)(1)
	private val fd = fs.openSync(file.getPath, 'r')

	override def close = fs.closeSync(fd)

	/*
	 * Returns the next byte of data or -1 if the end of the file was reached
	 */
	def read(): Int = {
		val bytesRead = fs.readSync(fd, bf, 0, 1, null).asInstanceOf[Int]
		/*
		 * fd, buffer, offset, length, position
		 * position is an integer specifying where to begin reading from in the file. If position is null, data will be read from the current file position.
		 */
		if (bytesRead == 1) {
			// bf(0).asInstanceOf[Int]
			bf.readUInt8(0).asInstanceOf[Int]
		} else {
			-1
		}
	}
}