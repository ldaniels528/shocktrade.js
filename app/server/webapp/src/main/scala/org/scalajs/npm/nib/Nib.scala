package org.scalajs.npm.nib

import org.scalajs.nodejs.{NodeModule, NodeRequire}

import scala.scalajs.js

/**
  * Nib - Stylus mixins, utilities, components, and gradient image generation.
  * @see [[https://www.npmjs.com/package/nib]]
  * @version 1.1.2
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait Nib extends NodeModule {

  def apply(): js.Function1[Style, Unit] = js.native

}

/**
  * Nib Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Nib {

  def apply()(implicit require: NodeRequire) = require[Nib]("nib")

}