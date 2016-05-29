package fiddle

import scala.reflect.io.{VirtualDirectory, Streamable, Path}
import java.util.zip.ZipInputStream
import java.io._
import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._
import org.scalajs.dom._
import scala.collection.immutable.Traversable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.typedarray.{Uint8Array, Int8Array, ArrayBufferInputStream}

/**
 * Loads the jars that make up the classpath of the scala-js-fiddle
 * compiler and re-shapes it into the correct structure to satisfy
 * scala-compile and scalajs-tools
 */
class Classpath {
  /**
   * In memory cache of all the jars used in the compiler. This takes up some
   * memory but is better than reaching all over the filesystem every time we
   * want to do something.
   */
  def filesReady = virtualDirectories.length == numberOfFilesToLoad

  def getVirtualDirectories: Option[Seq[VirtualDirectory]] = {
    if (filesReady) {
      Some(scala.collection.immutable.Seq[VirtualDirectory](virtualDirectories: _*))
    } else {
      None
    }
  } 

  private var virtualDirectories = new ArrayBuffer[VirtualDirectory]()

  private val paths = Seq(
    "/home/roger/EPFL-MA/Projet_II/scala-library-2.11.7.jar",
    "/home/roger/EPFL-MA/Projet_II/scalajs-library_2.11-0.6.9.jar",
    "/home/roger/EPFL-MA/Projet_II/rt.jar"
  )

  private val numberOfFilesToLoad = paths.length

  for (path <- paths) {
    val xhr = new XMLHttpRequest()
    xhr.open("GET", "file://" + path)
    xhr.responseType = "arraybuffer"
    xhr.onload = ((e: Event) => {
      val array = new Uint8Array(xhr.response.asInstanceOf[js.Array[Int]])
      println(s"Loading $path (${array.length} bytes)")
      val buffer = array.buffer
      val inputStream: InputStream = new ArrayBufferInputStream(buffer)
      val bytes = Streamable.bytes(inputStream)
      val in = new ZipInputStream(new ByteArrayInputStream(bytes))
      val entries = Iterator
        .continually(in.getNextEntry)
        .takeWhile(_ != null)
        .map((_, Streamable.bytes(in)))
      val dir = new VirtualDirectory(path, None)
      for {
        (e, data) <- entries
        if !e.isDirectory
      } {
        val tokens = e.getName.split("/")
        var d = dir
        for(t <- tokens.dropRight(1)) {
          d = d.subdirectoryNamed(t).asInstanceOf[VirtualDirectory]
        }
        val f = d.fileNamed(tokens.last)
        val o = f.bufferedOutput
        o.write(data)
        o.close()
      }
      // println(dir.size)
      virtualDirectories += dir
      println(s"${path.split("/").last} loaded")
    })
    xhr.send()
  }
}
//    val paths = List("C:/Program Files/Java/jre1.8.0_74/lib/rt.jar")
// /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.77-1.b03.fc23.x86_64/jre/lib/rt.jar
//     val paths = List("/home/roger/EPFL-MA/Projet_II/rt.jar")
//     val bootFiles = for {
//       // les System properties ne sont pas accessibles par Scala.js 
// //      prop <- Seq(/*"java.class.path", */"sun.boot.class.path")
// //      path <- System.getProperty(prop).split(System.getProperty("path.separator"))
//     	path <- paths
//       vfile = scala.reflect.io.File(path)
//       if vfile.exists && !vfile.isDirectory
//     } yield {
//       path.split("/").last -> vfile.toByteArray()
//     }
//     println("Files loaded...")
//     jarFiles ++ bootFiles
  // }
  /**
   * The loaded files shaped for Scalac to use
   */
//   val scalac = for((name, bytes) <- loadedFiles) yield {
//     println(s"Loading $name for Scalac")
//     val in = new ZipInputStream(new ByteArrayInputStream(bytes))
//     val entries = Iterator
//       .continually(in.getNextEntry)
//       .takeWhile(_ != null)
//       .map((_, Streamable.bytes(in)))

//     val dir = new VirtualDirectory(name, None)
//     for {
//       (e, data) <- entries
//       if !e.isDirectory
//     } {
//       val tokens = e.getName.split("/")
//       var d = dir
//       for(t <- tokens.dropRight(1)) {
//         d = d.subdirectoryNamed(t).asInstanceOf[VirtualDirectory]
//       }
//       val f = d.fileNamed(tokens.last)
//       val o = f.bufferedOutput
//       o.write(data)
//       o.close()
//     }
//     // println(dir.size)
//     dir
//   }

// }