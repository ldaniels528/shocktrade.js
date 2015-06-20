package com.shocktrade.javascript.directives

import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON

/**
 * Avatar Directive
 * @author lawrence.daniels@gmail.com
 *         <avatar id="{{ p.facebookID }}" class="avatar-24"></avatar>
 */
object AvatarDirective {

  def init() {
    val app = g.angular.module("shocktrade")

    val updateScope = { (newValue: js.Any, oldValue: js.Any, scope: js.Dynamic) =>
      if (isDefined(scope.id)) {
        scope.url = s"http://graph.facebook.com/${scope.id}/picture"
        scope.myClass = "playerAvatar"
      }
      else if (isDefined(scope.`object`)) {
        scope.myClass = "playerAvatar"
        val json = JSON.parse(scope.`object`.as[String])
        if (!isDefined(json)) scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
        else if (isDefined(json.picture)) scope.url = json.picture.data.url
        else if (isDefined(json.facebookID)) scope.url = s"http://graph.facebook.com/${json.facebookID}/picture"
        else {
          g.console.error(s"Avatar object type could not be determined - ${scope.`object`}")
          scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
        }
      }
      else {
        scope.url = if (isDefined(scope.alt)) scope.alt.as[String] else "/assets/images/avatars/avatar100.png"
        scope.myClass = "spectatorAvatar"
      }
    }

    val linkFx = { (scope: js.Dynamic, element: js.Dynamic, attrs: js.Dynamic) =>
      scope.$watch("object", (newValue: js.Any, oldValue: js.Any) => updateScope(newValue, oldValue, scope))
      scope.$watch("id", (newValue: js.Any, oldValue: js.Any) => updateScope(newValue, oldValue, scope))
    }

    app.directive("avatar", js.Array({ () =>
      JS(
        "restrict" -> "E",
        "scope" -> JS(id = "@id", `object` = "@object", alt = "@alt", `class` = "@class", style = "@style"),
        "transclude" -> true,
        "replace" -> false,
        "template" -> """<img ng-src="{{ url }}" ng-class="myClass" class="{{ class }}" style="{{ style }}">""",
        "link" -> linkFx
      )
    }: js.Function0[js.Dynamic]))
  }

}
