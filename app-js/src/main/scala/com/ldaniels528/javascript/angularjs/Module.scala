package com.ldaniels528.javascript.angularjs

import com.ldaniels528.javascript.angularjs.Service
import ScalaJsHelper._

import scala.scalajs.js

/**
 * Represents an Angular.js Module
 * @author lawrence.daniels@gmail.com
 * @see [[https://docs.angularjs.org/api/ng/type/angular.Module]]
 */
trait Module extends js.Object {

  def name: String = js.native

  def animation(name: String, animationFactory: js.Array[Any]): Module = js.native

  def config(dependencies: js.Array[Any]): Module = js.native

  def constant(name: String, value: js.Any): Module = js.native

  def controller(name: String, constructor: js.Array[Any]): Module = js.native

  def directive(name: String, directiveFactory: js.Array[Any]): Module = js.native

  def directive(name: String, directiveFactory: js.Function): Module = js.native

  def factory(name: String, constructor: js.Array[Any]): Module = js.native

  def filter(name: String, f: js.Function): Module = js.native

  def provider(name: String, constructor: js.Array[Any]): Module = js.native

  def run(f: js.Function): Module = js.native

  def service(name: String, constructor: js.Array[Any]): Module = js.native

}

/**
 * Module Singleton
 * @author lawrence.daniels@gmail.com
 */
object Module {

  implicit class ModuleExtensions(val module: Module) extends AnyVal {

    def controllerOf[T <: Controller](name: String) = module.controller(name, emptyArray)

    def serviceOf[T <: Service](name: String) = module.service(name, emptyArray)

  }

}
