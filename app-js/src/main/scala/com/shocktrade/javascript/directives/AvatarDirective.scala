package com.shocktrade.javascript.directives

import com.ldaniels528.scalascript.ScalaJsHelper._
import com.ldaniels528.scalascript.core.{Attributes, JQLite}
import com.ldaniels528.scalascript.{Directive, Scope}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON

/**
 * Avatar Directive
 * @author lawrence.daniels@gmail.com
 * @example <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
 */
class AvatarDirective extends Directive[AvatarDirectiveScope] {
  override val restrict = "E"
  override val scope = JS(id = "@id", `object` = "@object", alt = "@alt", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">"""

  def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("object", (newValue: js.Any, oldValue: js.Any) => populateScope(scope, newValue, oldValue))
    scope.$watch("id", (newValue: js.Any, oldValue: js.Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: js.Any, oldValue: js.Any) {
    if (isDefined(scope.id)) {
      scope.url = s"http://graph.facebook.com/${scope.id}/picture"
      scope.myClass = "playerAvatar"
    }
    else if (isDefined(scope.`object`)) {
      scope.myClass = "playerAvatar"
      val json = JSON.parse(scope.`object`)
      if (!isDefined(json)) scope.url = if (isDefined(scope.alt)) scope.alt else "/assets/images/avatars/avatar100.png"
      else if (isDefined(json.picture)) scope.url = json.picture.data.url.as[String]
      else if (isDefined(json.facebookID)) scope.url = s"http://graph.facebook.com/${json.facebookID}/picture"
      else {
        g.console.error(s"Avatar object type could not be determined - ${scope.`object`}")
        scope.url = if (isDefined(scope.alt)) scope.alt else "/assets/images/avatars/avatar100.png"
      }
    }
    else {
      scope.url = if (isDefined(scope.alt)) scope.alt else "/assets/images/avatars/avatar100.png"
      scope.myClass = "spectatorAvatar"
    }
  }

}

/**
 * Avatar Directive Scope
 * @author lawrence.daniels@gmail.com
 */
trait AvatarDirectiveScope extends Scope {
  var id: String = js.native
  var `object`: String = js.native
  var alt: String = js.native
  var `class`: String = js.native
  var style: String = js.native
  var url: String = js.native
  var myClass: String = js.native

}

/**
 * Avatar Directive Scope Singleton
 * @author lawrence.daniels@gmail.com
 */
object AvatarDirectiveScope {

  def apply(): AvatarDirectiveScope = new js.Object().asInstanceOf[AvatarDirectiveScope]

}