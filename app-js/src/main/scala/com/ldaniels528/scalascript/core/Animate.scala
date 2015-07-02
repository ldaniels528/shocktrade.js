// -   Project: scalajs-scalascript (https://github.com/jokade/scalajs-scalascript)
// Description: API for Angular $animate service
//
// Distributed under the MIT License (see included file LICENSE)
package com.ldaniels528.scalascript.core

import org.scalajs.dom.Element

import scala.scalajs.js

/**
 * API for Angular \$animate service.
 *
 * @see [[https://docs.angularjs.org/api/ng/service/\$animate]]
 */
trait AnimateService[T] extends ProvidedService {

  def enter(element: Element, parent: Element, after: Element): QPromise[T] = js.native

  def enter(element: Element, parent: Element, after: Element, options: js.Object): QPromise[T] = js.native

  def leave(element: Element): QPromise[T] = js.native

  def leave(element: Element, options: js.Object): QPromise[T] = js.native

  def move(element: Element, parent: Element, after: Element): QPromise[T] = js.native

  def move(element: Element, parent: Element, after: Element, options: js.Object): QPromise[T] = js.native

  def addClass(element: Element, className: String): QPromise[T] = js.native

  def addClass(element: Element, className: String, options: js.Object): QPromise[T] = js.native

  def removeClass(element: Element, className: String): QPromise[T] = js.native

  def removeClass(element: Element, className: String, options: js.Object): QPromise[T] = js.native

  def setClass(element: Element, add: String, remove: String): QPromise[T] = js.native

  def setClass(element: Element, add: String, remove: String, options: js.Object): QPromise[T] = js.native

}
