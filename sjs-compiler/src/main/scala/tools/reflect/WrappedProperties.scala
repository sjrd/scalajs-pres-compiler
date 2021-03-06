/* NSC -- new Scala compiler
 * Copyright 2006-2013 LAMP/EPFL
 * @author  Paul Phillips
 */

package scala.tools
package reflect

import scala.util.PropertiesTrait
import java.security.AccessControlException
import java.net.URL
import java.io.IOException
import scala.scalajs.js
import js.Dynamic.global
import js.DynamicImplicits._
import java.io.InputStream
import scala.scalajs.js.typedarray.{ Uint8Array, Int8Array, ArrayBufferInputStream }

/**
 * For placing a wrapper function around property functions.
 *  Motivated by places like google app engine throwing exceptions
 *  on property lookups.
 */
trait WrappedProperties extends PropertiesTrait {
  def wrap[T](body: => T): Option[T]

  protected def propCategory = "wrapped"
  protected def pickJarBasedOn = this.getClass // comment in PropertiesTrait : props file comes from jar containing this
  
  /*
   * There is a getResourceAsStream in scala.util.PropertiesTrait, which does not work
   * with Scala.js. Therefore we override the scalaProps val containing the
   * getResourceAsStream call, and simply return an empty java.util.Properties
   */
  override protected lazy val scalaProps: java.util.Properties = {
    val props = new java.util.Properties
    props.setProperty("version.number", "2.11.7") /* does not seem to do anything later on, hardcoded version
    * 2.11.7 in tools.nsc.settings.ScalaVersion.scala, line 119, instead
    */
    props
  }

  override def propIsSet(name: String) = wrap(super.propIsSet(name)) exists (x => x)
  override def propOrElse(name: String, alt: String) = wrap(super.propOrElse(name, alt)) getOrElse alt
  override def setProp(name: String, value: String) = wrap(super.setProp(name, value)).orNull
  override def clearProp(name: String) = wrap(super.clearProp(name)).orNull
  override def envOrElse(name: String, alt: String) = wrap(super.envOrElse(name, alt)) getOrElse alt
  override def envOrNone(name: String) = wrap(super.envOrNone(name)).flatten
  override def envOrSome(name: String, alt: Option[String]) = wrap(super.envOrNone(name)).flatten orElse alt

  def systemProperties: List[(String, String)] = {
    import scala.collection.JavaConverters._
    wrap {
      val props = System.getProperties
      // SI-7269 Be careful to avoid `ConcurrentModificationException` if another thread modifies the properties map
      props.stringPropertyNames().asScala.toList.map(k => (k, props.get(k).asInstanceOf[String]))
    } getOrElse Nil
  }
}

object WrappedProperties {
  object AccessControl extends WrappedProperties {
    def wrap[T](body: => T) = try Some(body) catch { case _: AccessControlException => None }
  }
}