package com.ldaniels528.javascript.angularjs.core

import org.scalajs.dom.html.{Document, Element}
import org.scalajs.jquery.JQuery

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

/**
 * Angular.js Object
 * @author lawrence.daniels@gmail.com
 */
trait Angular extends js.Object {

  def bootstrap(element: String, modules: String): Injector = js.native

  def bootstrap(element: String, modules: js.Array[Any]): Injector = js.native

  def bootstrap(element: String, modules: js.Array[Any], config: AngularConfig): Injector = js.native

  def bootstrap(element: String, modules: String, config: AngularConfig): Injector = js.native

  def bootstrap(element: Element, modules: js.Array[Any]): Injector = js.native

  def bootstrap(element: Element, modules: js.Array[Any], config: AngularConfig): Injector = js.native

  def bootstrap(element: Element, modules: String): Injector = js.native

  def bootstrap(element: Element, modules: String, config: AngularConfig): Injector = js.native

  def bootstrap(element: Document, modules: js.Array[Any]): Injector = js.native

  def bootstrap(element: Document, modules: js.Array[Any], config: AngularConfig): Injector = js.native

  def bootstrap(element: Document, modules: String): Injector = js.native

  def bootstrap(element: Document, modules: String, config: AngularConfig): Injector = js.native

  def bootstrap(element: js.Any, modules: js.Array[Any]): Injector = js.native

  def bootstrap(element: js.Any, modules: js.Array[Any], config: AngularConfig): Injector = js.native

  def bootstrap(element: js.Any, modules: String): Injector = js.native

  def bootstrap(element: js.Any, modules: String, config: AngularConfig): Injector = js.native

  def bootstrap(element: JQuery, modules: js.Array[Any]): Injector = js.native

  def bootstrap(element: JQuery, modules: js.Array[Any], config: AngularConfig): Injector = js.native

  def bootstrap(element: JQuery, modules: String): Injector = js.native

  def bootstrap(element: JQuery, modules: String, config: AngularConfig): Injector = js.native

  def element(jquery: JQuery): AngularJQLite = js.native

  def element(ref: String): AngularJQLite = js.native

  def injector(modules: js.Any, strictDi: Boolean = false): Injector = js.native

  def module(name: String): Module = js.native

  def module(name: String, requires: js.Array[String]): Module = js.native

  def module(name: String, requires: js.Array[String], configFn: js.Array[Any]): Module = js.native

  def reloadWithDebugInfo(): Unit = js.native

  def toJson(obj: js.Any, pretty: js.Any = false): String = js.native

  def uppercase(s: String): String = js.native

  val version: AngularVersion = js.native

}

/**
 * Angular.js JQuery Function
 * @author lawrence.daniels@gmail.com
 */
trait AngularJQLite extends js.Object {

  def find(element: String): AngularJQLite = js.native

  def find(element: js.Any): AngularJQLite = js.native

  //def find(obj: js.Any): AngularJQLite = js.native

  def controller(): js.Any = js.native

  def controller(name: String): js.Any = js.native

  def injector(): Injector = js.native

  def scope(): Scope = js.native

  def isolateScope(): Scope = js.native

  def inheritedData(key: String, value: js.Any): Q = js.native

  def inheritedData(obj: js.Dictionary[Any]): Q = js.native

  def inheritedData(key: String): js.Any = js.native

  def inheritedData(): js.Any = js.native

}

/**
 * Angular.js Configuration
 * @author lawrence.daniels@gmail.com
 */
trait AngularConfig extends js.Object {
  var strictDi: Boolean = false

}

/**
 * Angular.js Configuration Singleton
 * @author lawrence.daniels@gmail.com
 */
object AngularConfig {

  def apply(strictDi: Boolean = false): AngularConfig = {
    val config = new js.Object().asInstanceOf[AngularConfig]
    config.strictDi = strictDi
    config
  }

}

/**
 * Angular.js Version Information
 * @author lawrence.daniels@gmail.com
 */
trait AngularVersion extends js.Object {
  val codeName: String = js.native
  val dot: Int = js.native
  val full: String = js.native
  val major: Int = js.native
  val minor: Int = js.native

}

/**
 * Angular.js Module Singleton
 * @author lawrence.daniels@gmail.com
 */
object Angular {
  val angular: Angular = apply()

  /**
   * Returns the global Angular object
   */
  def apply(): Angular = g.angular.asInstanceOf[js.UndefOr[Angular]].getOrElse(throw new RuntimeException("The Angular.js library not found"))

}
