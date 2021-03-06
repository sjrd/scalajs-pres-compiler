package fiddle

import scala.tools.nsc.Settings
import scala.reflect.io
import scala.tools.nsc.util._
import java.io._
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.plugins.Plugin
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.reflect.internal.util.{ BatchSourceFile, OffsetPosition }
import scala.tools.nsc.interactive.{ InteractiveAnalyzer, Response }
import scala.tools.nsc
import scala.io.Source
import scala.tools.nsc.backend.JavaPlatform
import scala.tools.nsc.util.ClassPath.JavaContext
import scala.collection.mutable
import scala.tools.nsc.typechecker.Analyzer
import scala.Some
import scala.util.{ Success, Failure }

/**
 * Handles the interaction between scala-js-fiddle and
 * scalac/scalajs-tools to compile and optimize code submitted by users.
 */
object Compiler {

  val blacklist = Seq("<init>")

  /**
   * Converts Scalac's weird Future type
   * into a standard scala.concurrent.Future
   */
  def toFuture[T](func: Response[T] => Unit): Future[T] = {
    val r = new Response[T]
    Future { func(r); r.get.left.get }
  }

  /**
   * Converts a bunch of bytes into Scalac's weird VirtualFile class
   */
  def makeFile(src: Array[Byte]) = {
    val singleFile = new io.VirtualFile("Main.scala")
    val output = singleFile.output
    output.write(src)
    output.close()
    singleFile
  }

  def inMemClassloader = {
    new ClassLoader(this.getClass.getClassLoader) {
      val classCache = mutable.Map.empty[String, Option[Class[_]]]
      override def findClass(name: String): Class[_] = {
        println("Looking for Class " + name)
        val fileName = name.replace('.', '/') + ".class"
        val res = classCache.getOrElseUpdate(
          name,
          Classpath.scalac
            .map(_.lookupPathUnchecked(fileName, false))
            .find(_ != null).map { f =>
              val data = f.toByteArray
              this.defineClass(name, data, 0, data.length)
            })
        res match {
          case None =>
            println("Not Found Class " + name)
            throw new ClassNotFoundException()
          case Some(cls) =>
            println("Found Class " + name)
            cls
        }
      }
    }
  }
  /**
   * Mixed in to make a Scala compiler run entirely in-memory,
   * loading its classpath and running macros from pre-loaded
   * in-memory files
   */
  trait InMemoryGlobal { g: scala.tools.nsc.Global =>
    def ctx: JavaContext
    def dirs: Vector[DirectoryClassPath]
    //    override lazy val plugins = List[Plugin](new org.scalajs.core.compiler.ScalaJSPlugin(this))
    override lazy val platform: ThisPlatform = new JavaPlatform {
      val global: g.type = g
      override def classPath = new JavaClassPath(dirs, ctx)
    }

  }

  /**
   * Code to initialize random bits and pieces that are needed
   * for the Scala compiler to function, common between the
   * normal and presentation compiler
   */
  def initGlobalBits(logger: String => Unit) = {
    val vd = new io.VirtualDirectory("(memory)", None)
    val jCtx = new JavaContext()
    val jDirs = Classpath.scalac.map(new DirectoryClassPath(_, jCtx)).toVector
    lazy val settings = new Settings

    settings.outputDirs.setSingleOutput(vd)
    
    /* java.io.Writer is an abstract class, "new Writer {...}" is a shortcut to create an anonymous class that
     * extends the abstract class and instantiate it in one step :
     * http://stackoverflow.com/questions/16259168/how-does-curly-braces-following-trait-instantiation-work/16259734
     * http://stackoverflow.com/questions/9762338/scala-abstract-classes-instantiation
     */
    val writer = new Writer {
      var inner = new StringWriter
      def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
        inner.append(cbuf, off, off + len + 1)
        // +1 because end = the index of the character following the last character in the subsequence
        // len is the number of chars to write including the char starting at the off
        // before : inner = inner ++ ByteString.fromArray(cbuf.map(_.toByte), off, len)
      }
      def flush(): Unit = {
        logger(inner.toString)
        inner = new StringWriter
      }
      def close(): Unit = ()
    }
    val reporter = new ConsoleReporter(settings, scala.Console.in, new PrintWriter(writer))
    (settings, reporter, vd, jCtx, jDirs)

  }

  def autocomplete(code: String, flag: String, pos: Int): Future[List[(String, String)]] = {
    // global can be reused, just create new runs for new compiler invocations
    val (settings, reporter, vd, jCtx, jDirs) = initGlobalBits(_ => ())
    val compiler = new nsc.interactive.Global(settings, reporter) with InMemoryGlobal { g =>
      def ctx = jCtx
      def dirs = jDirs
      override lazy val analyzer = new {
        val global: g.type = g
      } with InteractiveAnalyzer {
        // val cl = inMemClassloader
        // override def findMacroClassLoader() = cl
      }
    }

    val file = new BatchSourceFile(makeFile(Shared.prelude.getBytes ++ code.getBytes), Shared.prelude + code)
    val position = new OffsetPosition(file, pos + Shared.prelude.length)

    val reloadFuture = toFuture[Unit](compiler.askReload(List(file), _))

    val compilerMembersFuture = reloadFuture flatMap {
      _ =>
        {

          toFuture[List[compiler.Member]](flag match {
            case "scope" => compiler.askScopeCompletion(position, _: compiler.Response[List[compiler.Member]])
            case "member" => compiler.askTypeCompletion(position, _: compiler.Response[List[compiler.Member]])
          })
        }
    }

    compilerMembersFuture map {
      listMembers =>
        {
          val res = compiler.ask { () =>

            def sig(x: compiler.Member) = {
              Seq(
                x.sym.signatureString,
                s" (${x.sym.kindString})").find(_ != "").getOrElse("--Unknown--")
            }

            listMembers.map((x: compiler.Member) => sig(x) -> x.sym.decodedName).filter(!blacklist.contains(_)).distinct
          }
          compiler.askShutdown()
          res
        }
    }
  }
}