import org.scalatest.FunSuite
import java.io.{File, FileInputStream}
import scala.collection.mutable.ListBuffer

class FileInputStreamTests extends FunSuite {

	test("read test") {
		val file = new File("/tmp/test.txt")
		val is = new FileInputStream(file)

		var i: Int = 0
		var text = new ListBuffer[Char]()

		while ({i = is.read(); i != -1 }) {
			text += i.toChar
		}

		println(text.mkString)
		assert(true)
	}

}