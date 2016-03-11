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
    val jarFiles = for {
      name <- Seq(
        "C:/Users/Roger/Projet_II/scala-library-2.11.7.jar",
        "C:/Users/Roger/Projet_II/scalajs-library_2.11-0.6.7"
      )
    } yield {
      val fs = global.require("fs")
      val file = fs.readFileSync(name).asInstanceOf[InputStream]
//      val stream = getClass.getResourceAsStream(name)
//      println("Loading file" + name + ": " + stream)
//      if (stream == null) {
//        throw new Exception(s"Classpath loading failed, jar $name not found")
//      }
      name -> Streamable.bytes(file)
    }
    
    val paths = List("C:/Program Files/Java/jre1.8.0_74/lib/rt.jar")
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
    for{
      (e, data) <- entries
      if !e.isDirectory
    } {
      val tokens = e.getName.split("/")
      var d = dir
      for(t <- tokens.dropRight(1)){
        d = d.subdirectoryNamed(t).asInstanceOf[VirtualDirectory]
      }
      val f = d.fileNamed(tokens.last)
      val o = f.bufferedOutput
      o.write(data)
      o.close()
    }
    println(dir.size)
    dir
  }
  /**
   * The loaded files shaped for Scala-Js-Tools to use
   */
  // lazy val scalajs = {
  //   println("Loading scalaJSClassPath")
  //   class Builder extends AbstractJarLibClasspathBuilder{
  //     val DummyVersion = "DUMMY_FILE"
  //     def listFiles(d: File) = Nil
  //     def toJSFile(f: File) = {
  //       val file = new MemVirtualJSFile(f._1)
  //       file.content = new String(f._2)
  //       file
  //     }
  //     def toIRFile(f: File) = {
  //       val file = new MemVirtualSerializedScalaJSIRFile(f._1)
  //       file.content = f._2
  //       file
  //     }
  //     def isDirectory(f: File) = false
  //     type File = (String, Array[Byte])
  //     def toInputStream(f: File) = new ByteArrayInputStream(f._2)
  //     def isFile(f: File) = true
  //     def isJSFile(f: File) = f._1.endsWith(".js")
  //     def isJARFile(f: File) = f._1.endsWith(".jar")
  //     def exists(f: File) = true
  //     def getName(f: File) = f._1
  //     def isIRFile(f: File) = f._1.endsWith(".sjsir")
  //     def getVersion(f: File) = Random.nextInt().toString
  //     def getAbsolutePath(f: File) = f._1
  //     def toReader(f: File) = new InputStreamReader(toInputStream(f))
  //   }

  //   val res = loadedFiles.map(new Builder().build(_))
  //                        .reduceLeft(_ merge _)
  //   println("Loaded scalaJSClassPath")
  //   res
  // }
}
