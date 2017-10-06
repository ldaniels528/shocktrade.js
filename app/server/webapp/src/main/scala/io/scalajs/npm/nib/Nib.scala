package io.scalajs.npm.nib

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Nib - Stylus mixins, utilities, components, and gradient image generation.
  * @see [[https://www.npmjs.com/package/nib]]
  * @version 1.1.2
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
@JSImport("nib", JSImport.Namespace)
object Nib extends js.Object {

  def apply(): js.Function1[Style, Unit] = js.native

}
