package com.shocktrade.client.directives

import io.scalajs.npm.angularjs.Directive._
import io.scalajs.npm.angularjs._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Avatar Directive
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 * @example <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
 */
class AvatarDirective extends Directive with ElementRestriction with LinkSupport[AvatarDirectiveScope] with TemplateSupport {
  private val UNKNOWN_PERSON = "/images/avatars/avatar100.png"
  override val scope = new AvatarDirectiveInputs(id = "@id", link = "@link", `class` = "@class", style = "@style")
  override val template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
    scope.$watch("link", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: Any, oldValue: Any): Unit = {
    val fixBlanks: String => js.UndefOr[String] = (s: String) => s.trim match {
      case s if s.isEmpty => js.undefined
      case s => s
    }

    // determine the image URL
    scope.url = scope.id.flatMap(fixBlanks).map(id => s"/api/user/icon/$id") ?? UNKNOWN_PERSON

    // set the class
    scope.myClass = if (scope.url.contains(UNKNOWN_PERSON)) "spectatorAvatar" else "playerAvatar"
  }
}

/**
 * Avatar Directive Input Parameters
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AvatarDirectiveInputs(val id: js.UndefOr[String], val link: js.UndefOr[String], val `class`: String, val style: String) extends js.Object

/**
 * Avatar Directive Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait AvatarDirectiveScope extends AvatarDirectiveInputs with Scope {
  // output fields
  var myClass: js.UndefOr[String] = js.native
  var url: js.UndefOr[String] = js.native

}
