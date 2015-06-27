package com.ldaniels528.javascript.angularjs.core

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/**
 * Angular.js JQLite
 * @author lawrence.daniels@gmail.com
 */
trait JQLite extends js.Object {

  def addClass(classNames: String): JQLite = js.native

  def addClass(func: js.Function2[js.Any, js.Any, JQLite]): js.Dynamic = js.native

  def attr(attributeName: String): String = js.native

  def attr(attributeName: String, value: js.Any): JQLite = js.native

  def attr(map: js.Any): JQLite = js.native

  def attr(attributeName: String, func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  def hasClass(className: String): Boolean = js.native

  def html(htmlString: String): JQLite = js.native

  def html(): String = js.native

  def prop(propertyName: String): js.Dynamic = js.native

  def prop(propertyName: String, value: js.Any): JQLite = js.native

  def prop(map: js.Any): JQLite = js.native

  def prop(propertyName: String, func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  def removeAttr(attributeName: js.Any): JQLite = js.native

  def removeClass(className: js.Any): JQLite = js.native

  def removeClass(): JQLite = js.native

  def removeClass(func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  def toggleClass(className: js.Any, swtch: Boolean): JQLite = js.native

  def toggleClass(className: js.Any): JQLite = js.native

  def toggleClass(swtch: Boolean): JQLite = js.native

  def toggleClass(): JQLite = js.native

  def toggleClass(func: js.Function3[js.Any, js.Any, js.Any, js.Any]): JQLite = js.native

  def `val`(): js.Dynamic = js.native

  def `val`(value: js.Array[String]): JQLite = js.native

  def `val`(value: String): JQLite = js.native

  def `val`(func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  @JSName("val") def value(): js.Dynamic = js.native

  @JSName("val") def value(value: js.Array[String]): JQLite = js.native

  @JSName("val") def value(value: String): JQLite = js.native

  @JSName("val") def value(func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  def css(propertyNames: js.Array[js.Any]): String = js.native

  def css(propertyName: String): String = js.native

  def css(propertyName: String, value: js.Any): JQLite = js.native

  def css(propertyName: js.Any, value: js.Any): JQLite = js.native

  def css(propertyName: js.Any): JQLite = js.native

  def data(key: String, value: js.Any): JQLite = js.native

  def data(obj: js.Any): JQLite = js.native

  def data(key: String): js.Dynamic = js.native

  def data(): js.Dynamic = js.native

  def removeData(nameOrList: js.Any): JQLite = js.native

  def removeData(): JQLite = js.native

  def bind(eventType: String, preventBubble: Boolean): JQLite = js.native

  def bind(events: js.Any*): js.Dynamic = js.native

  def off(events: String): JQLite = js.native

  def off(): JQLite = js.native

  def off(eventsMap: js.Any): JQLite = js.native

  def on[T <: Event](events: String, handler: js.Function1[T, _]): JQLite = js.native

  def on[T <: Event](eventsMap: js.Any, handler: js.Function1[T, _]): JQLite = js.native

  def one[T <: Event](events: String, handler: js.Function1[T, _]): JQLite = js.native

  def one[T <: Event](eventsMap: js.Any, handler: js.Function1[T, _]): JQLite = js.native

  def ready(handler: js.Any): JQLite = js.native

  def triggerHandler(eventType: String, extraParameters: js.Any*): Object = js.native

  def unbind(eventType: String): JQLite = js.native

  def unbind(): JQLite = js.native

  def unbind(eventType: String, fls: Boolean): JQLite = js.native

  def unbind(evt: js.Any): JQLite = js.native

  def after(content: js.Any*): JQLite = js.native

  def after(func: js.Function1[js.Any, js.Any]): js.Dynamic = js.native

  def append(content: js.Any*): JQLite = js.native

  def append(func: js.Function2[js.Any, js.Any, js.Any]): js.Dynamic = js.native

  def clone(withDataAndEvents: Boolean, deepWithDataAndEvents: Boolean): JQLite = js.native

  def clone(withDataAndEvents: Boolean): JQLite = js.native

  override def clone(): JQLite = js.native

  def detach(selector: js.Any): JQLite = js.native

  def detach(): JQLite = js.native

  def empty(): JQLite = js.native

  def prepend(content: js.Any*): JQLite = js.native

  def prepend(func: js.Function2[js.Any, js.Any, js.Any]): JQLite = js.native

  def remove(selector: js.Any): JQLite = js.native

  def remove(): JQLite = js.native

  def replaceWith(func: js.Any): JQLite = js.native

  def text(textString: String): JQLite = js.native

  def text(): String = js.native

  def wrap(wrappingElement: js.Any): JQLite = js.native

  def wrap(func: js.Function1[js.Any, js.Any]): JQLite = js.native

  @js.annotation.JSBracketAccess
  def apply(x: Int): dom.html.Element = js.native

  def children(selector: js.Any): JQLite = js.native

  def children(): JQLite = js.native

  def contents(): JQLite = js.native

  def eq(index: Int): JQLite = js.native

  def find(selector: String): JQLite = js.native

  def next(): JQLite = js.native

  def parent(): JQLite = js.native

}