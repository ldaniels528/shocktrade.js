package com.shocktrade.javascript.directives

import biz.enef.angulate.Directive
import biz.enef.angulate.core.{Attributes, JQLite}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON

/**
 * Avatar Directive
 * @author lawrence.daniels@gmail.com
 *         <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
 */
class AvatarDirective extends Directive {
  override type ScopeType = js.Dynamic
  override type ControllerType = js.Dynamic
  override val restrict = "E"
  override val transclude = true
  override val replace = false
  override val template = """<img ng-src="{{ url }}" ng-class="ngClass" class="{{ class }}" style="{{ style }}">"""

  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes, controller: js.Dynamic): Unit = {
    g.console.info("I'm in postLink")

    scope.$watch("object", (newValue: js.Dynamic, oldValue: js.Dynamic) => {
      g.console.info(s"object: newValue = $newValue, oldValue = $oldValue")
      g.console.info(s"id = ${scope.id}, object = ${scope.`object`}, ngClass = ${scope.ngClass}, class = ${scope.`class`}")
      setURL(scope)
    })
    scope.$watch("id", (newValue: js.Dynamic, oldValue: js.Dynamic) => {
      g.console.info(s"id: newValue = $newValue, oldValue = $oldValue")
      g.console.info(s"id = ${scope.id}, object = ${scope.`object`}, ngClass = ${scope.ngClass}, class = ${scope.`class`}")
      setURL(scope)
    })
  }

  def setURL(scope: js.Dynamic) {
    if (isDefined(scope.id)) {
      scope.url = s"http://graph.facebook.com/${scope.id}/picture"
      scope.ngClass = "playerAvatar"
    }
    else if (isDefined(scope.`object`)) {
      val json = JSON.parse(scope.`object`.as[String])
      if (!isDefined(json)) scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
      else if (isDefined(json.picture)) scope.url = json.picture.data.url
      else if (isDefined(json.facebookID)) scope.url = s"http://graph.facebook.com/${json.facebookID}/picture"
      else {
        g.console.error(s"Avatar object type could not be determined - ${scope.`object`}")
        scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
      }
      scope.ngClass = "playerAvatar"
    }
    else {
      scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
      scope.ngClass = "spectatorAvatar"
    }
  }
}
