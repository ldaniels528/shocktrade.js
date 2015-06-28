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

  def bind(self: js.Any, fn: js.Function, args: js.Any): js.Function = js.native

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

  /**
   * @see https://docs.angularjs.org/api/ng/function/angular.copy
   */
  def copy(source: js.Any, destination: js.Any = js.undefined): js.Any = js.native

  def element(jquery: JQuery): AngularJQLite = js.native

  def element(ref: String): AngularJQLite = js.native

  def equals(o0: js.Any, o1: js.Any): Boolean = js.native

  def injector(modules: js.Any, strictDi: Boolean = false): Injector = js.native

  def isArray(value: js.Any): Boolean = js.native

  def isDate(value: js.Any): Boolean = js.native

  def isDefined(value: js.Any): Boolean = js.native

  def isElement(value: js.Any): Boolean = js.native

  def isFunction(value: js.Any): Boolean = js.native

  def isNumber(value: js.Any): Boolean = js.native

  def isObject(value: js.Any): Boolean = js.native

  def isString(value: js.Any): Boolean = js.native

  def isUndefined(value: js.Any): Boolean = js.native

  def lowercase(value: String): String = js.native

  def merge(dst: js.Any, src: js.Any): js.Any = js.native

  def module(name: String): Module = js.native

  def module(name: String, requires: js.Array[String]): Module = js.native

  def module(name: String, requires: js.Array[String], configFn: js.Array[Any]): Module = js.native

  def noop(): Unit = js.native

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
 * Angular.js Module Singleton
 * @author lawrence.daniels@gmail.com
 */
object Angular {
  val angular: Angular = apply()

  /**
   * Returns the global Angular object
   */
  def apply(): Angular = g.angular
    .asInstanceOf[js.UndefOr[Angular]]
    .getOrElse(throw new RuntimeException("The Angular.js library not found"))

}
