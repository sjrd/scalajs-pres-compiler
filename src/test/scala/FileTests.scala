import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import java.io.File

class FileTests extends FunSuite with BeforeAndAfter {
	
	before {
		
	}
	
	test("exists test") {
		var file = new File("/home/unknown")
//		assert(!file.exists)
	}
	
}