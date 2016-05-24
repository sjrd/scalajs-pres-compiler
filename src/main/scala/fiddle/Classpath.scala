package fiddle

import scala.reflect.io.{VirtualDirectory, Streamable, Path}
import java.util.zip.ZipInputStream
import java.io._
import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._
// import org.scalajs.core.tools.classpath.builder.{AbstractJarLibClasspathBuilder, PartialClasspathBuilder, JarTraverser}
// import org.scalajs.core.tools.io._
import scala.collection.immutable.Traversable
import scala.util.Random
import scala.scalajs.js.typedarray.{Uint8Array, Int8Array, ArrayBufferInputStream}

/**
 * Loads the jars that make up the classpath of the scala-js-fiddle
 * compiler and re-shapes it into the correct structure to satisfy
 * scala-compile and scalajs-tools
 */
object Classpath {
  /**
   * In memory cache of all the jars used in the compiler. This takes up some
   * memory but is better than reaching all over the filesystem every time we
   * want to do something.
   */
  lazy val loadedFiles = {
    println("Loading files...")
    val fs = global.require("fs")
    val jarFiles = for {
      name <- Seq(
          "/home/roger/EPFL-MA/Projet_II/scala-library-2.11.7.jar",
          "/home/roger/EPFL-MA/Projet_II/scalajs-library_2.11-0.6.9.jar"
//        "C:/Users/Roger/Projet_II/scala-library-2.11.7.jar",
//        "C:/Users/Roger/Projet_II/scalajs-library_2.11-0.6.7.jar"
      )
    } yield {
      // readFileSync returns a Node.js Buffer when encoding is not specified
      val file = fs.readFileSync(name)
      println(s"Number of bytes of file $name : ${file.length}") // this gives the correct size
      val buffer = new Uint8Array(fs.readFileSync(name).asInstanceOf[js.Array[Int]]).buffer
      val inputStream: InputStream = new ArrayBufferInputStream(buffer)
      name -> Streamable.bytes(inputStream)
    }
    
//    val paths = List("C:/Program Files/Java/jre1.8.0_74/lib/rt.jar")
// /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.77-1.b03.fc23.x86_64/jre/lib/rt.jar
    val paths = List("/home/roger/EPFL-MA/Projet_II/rt.jar")
    val bootFiles = for {
      // les System properties ne sont pas accessibles par Scala.js 
//      prop <- Seq(/*"java.class.path", */"sun.boot.class.path")
//      path <- System.getProperty(prop).split(System.getProperty("path.separator"))
    	path <- paths
      vfile = scala.reflect.io.File(path)
      if vfile.exists && !vfile.isDirectory
    } yield {
      path.split("/").last -> vfile.toByteArray()
    }
    println("Files loaded...")
    jarFiles ++ bootFiles
  }
  /**
   * The loaded files shaped for Scalac to use
   */
  lazy val scalac = for((name, bytes) <- loadedFiles) yield {
    println(s"Loading $name for Scalac")
    val in = new ZipInputStream(new ByteArrayInputStream(bytes))
    val entries = Iterator
      .continually(in.getNextEntry)
      .takeWhile(_ != null)
      .map((_, Streamable.bytes(in)))

    val dir = new VirtualDirectory(name, None)
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
      if (f.toString contains "ScalaSignature") { println(f) }
      val o = f.bufferedOutput
      o.write(data)
      o.close()
    }
    println(dir.size)
    dir
  }

}
